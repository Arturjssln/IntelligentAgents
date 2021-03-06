package bestagent;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.config.Parsers;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;


/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class AuctionAgent implements AuctionBehavior {

	// Different algorithms implemented
	enum Algorithm {AUCTION, NAIVE};
	enum Strategy {RISKY, HONNEST, SAFE, CATCHING_UP, VARIABLE};
	
	private Algorithm algorithm;
	private Strategy strategy; 
	private BidStrategy biddingStrategy;
	private Double epsilon;

	private long timeout_setup; 
	private long timeout_plan;
	private long timeout_bid;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<FashionVehicle> vehicles;
	private List<FashionVehicle> opponentVehicles; 
	private City currentCity; 
	private int maxCapacity; 

	private double ourCost;
	private double ourReward;
	private double opponentCost;
	private double opponentReward;
	private double ourNewCost;
	private double opponentNewCost;
	private List<Task> opponentTasks; 

	private List<Plan> newBestPlans; 
	private List<Plan> bestPlans; 

	private int round; 
	private int nbTasks;
	private Boolean useDistribution;


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		 // This code is used to get the timeouts
		 LogistSettings ls = null;
		 try {
			 ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
		 }
		 catch (Exception exc) {
			 System.out.println("There was a problem loading the configuration file.");
		 }

		String algorithmName = agent.readProperty("algorithm", String.class, "SLS");
		String strategyName = agent.readProperty("strategy", String.class, "VARIABLE");
		useDistribution = agent.readProperty("distribution", Boolean.class, true);
		epsilon = agent.readProperty("epsilon", Double.class, 0.1);


		// Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		System.out.println("Algorithm used : " + algorithm.toString());
		strategy = Strategy.valueOf(strategyName.toUpperCase());
		System.out.println("Strategy used : " + strategy.toString());
		
		System.out.println("Using distribution : " + useDistribution.toString());
		System.out.println("Epsilon used : " + epsilon.toString());

        // The setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // The plan method cannot execute more than timeout_plan milliseconds
		timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
		// The bid method cannot execute more than timeout_plan milliseconds
		timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);


		// Initialisation of the attributes 

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		initialiseVehiclesAndPlan();
		this.currentCity = vehicles.get(0).homeCity();
		initMaxCapacity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		this.round = 0; 
		this.nbTasks = 0; 
		this.ourReward = 0.0;
		this.ourCost = 0.0;
		this.ourNewCost = 0.0;
		this.opponentCost = 0.0;
		this.opponentNewCost = 0.0;
		this.opponentReward = 0.0; 
		this.opponentTasks = new ArrayList<Task>();

		initialiseStrategy();
	}
	
	private void initialiseVehiclesAndPlan() {
		// Initialise the agent's vehicles and their plans
		vehicles = new ArrayList<FashionVehicle>();
		opponentVehicles = new ArrayList<FashionVehicle>();
		bestPlans = new ArrayList<Plan>();
		for (Vehicle vehicle : agent.vehicles()) {
			vehicles.add(new FashionVehicle(vehicle));
			opponentVehicles.add(new FashionVehicle(vehicle));
			bestPlans.add(Plan.EMPTY);
		}
	}

	private void initMaxCapacity(){
		// Find the max capacity from all vehicles 
		int maxCapacity = 0; 
		for (FashionVehicle vehicle: this.vehicles) {
			maxCapacity = (vehicle.capacity() > maxCapacity) ? vehicle.capacity() : maxCapacity; 
		}
		this.maxCapacity = maxCapacity; 
	}
	
	private void initialiseStrategy() {
		// Initialise the dtrategy depending on the strategy name given in the .xml
		switch (strategy) {
			case SAFE:
				biddingStrategy = new SafeStrategy(epsilon);
				break;
			case HONNEST:
				biddingStrategy = new HonnestStrategy(epsilon);
				break;
			case RISKY:
			case VARIABLE:
				biddingStrategy = new RiskyStrategy(epsilon);
				break;
			case CATCHING_UP:
				biddingStrategy = new CatchingUpStrategy(epsilon);
				break;
			default: 
				throw new IllegalArgumentException("Strategy is invalid.");
		}
	}

	@Override
	public Long askPrice(Task task) {
		// Return the bid price of the company
		long startTime = System.currentTimeMillis();

		// Naive implementation 
		if (algorithm == Algorithm.NAIVE)
		return naiveBid(task);
		
		// If the task is too heavy for the company
		if (maxCapacity < task.weight) { return null; }

		// Compute costs with the extra task
		List<Task> possibleTasks = new ArrayList<Task>(agent.getTasks());
		ourNewCost = totalCost(possibleTasks, task, true, startTime); 
		opponentNewCost = totalCost(opponentTasks, task, false, startTime); 

		++round; 

		double ourDistributionRatio = computeDistributionRatio(task, true); 
		double opponentDistributionRatio = computeDistributionRatio(task, false); 

		Long bid = biddingStrategy.computeBid(ourNewCost-ourCost, opponentNewCost-opponentCost, ourDistributionRatio, opponentDistributionRatio); 
		
		System.out.println("Agent " + agent.id() + " bid " + bid + " new cost " + ourNewCost + " old cost " + ourCost);
		return bid;
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		// Treat info from the previous round

		// Naive implementation 
		if (algorithm == Algorithm.NAIVE)
			return;

		// Bids is comoposed of only 2 values
		ourReward += bids[agent.id()];
		Long opponentBid = bids[1 - agent.id()];
		opponentReward += opponentBid;

		if (round == 1) {
			estimateOpponentHomeCity(opponentBid, previous);
		}
		
		// Choose strategy depending on progression in the trial
		switch (strategy) {
			case VARIABLE: 
				if (nbTasks == 2) {
					biddingStrategy = new CatchingUpStrategy(epsilon);
				} else if (ourReward > ourNewCost && winner == agent.id()) {
					biddingStrategy = new SafeStrategy(epsilon);
				}
				break;
			default: //do nothing
		}

		// Wins 
		if (winner == agent.id()) {
			this.bestPlans = this.newBestPlans; 
			this.ourCost = this.ourNewCost; 
			++nbTasks;
			biddingStrategy.computeRiskRatio(true, round, nbTasks, ourCost, ourReward, opponentBid, opponentCost, opponentReward, opponentNewCost-opponentCost);
		}
		// Loses
		else {
			this.opponentTasks.add(previous);
			biddingStrategy.computeRiskRatio(false, round, nbTasks, ourCost, ourReward, opponentBid, opponentNewCost, opponentReward, opponentNewCost-opponentCost);	
			this.opponentCost = this.opponentNewCost;
		}
	}	

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		// Return final plan from all the tasks obtained
		long time_start = System.currentTimeMillis();

		// Naive implementation
		if (algorithm == Algorithm.NAIVE)
			return computeNaivePlans(vehicles, tasks);

		// Compute the plans for each vehicle with the selected algorithm
		List<FashionVehicle> fashionVehicles = new ArrayList<FashionVehicle>();
		for (Vehicle vehicle : vehicles) {
			fashionVehicles.add(new FashionVehicle(vehicle)); 
		}
		SLSAlgo algo = new SLSAlgo(fashionVehicles, new ArrayList<Task>(tasks));
		List<Plan> plans = algo.computePlans(time_start+timeout_plan, 10000);

        long time_end = System.currentTimeMillis();
		double duration = (time_end - time_start) / 1000.0;
		System.out.println("The plan was generated in " + duration + " seconds.");
        return plans;
	}

	private void estimateOpponentHomeCity(double opponentBid, Task task) {	
		// Returns the most probable Home cities for vehicles of the opponent

		double minDiffBid = Double.MAX_VALUE;
		double estimatedDistance = Double.MAX_VALUE;
		City estimatedHome = topology.cities().get(0); 
		for (City city: topology.cities()) {
			double distance = city.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
			double diffBid = Math.abs(distance * biddingStrategy.opponentRatio - opponentBid);
			if (diffBid < minDiffBid) {
				estimatedHome = city; 
				minDiffBid = diffBid; 
				estimatedDistance = distance;
			}
		}
		opponentVehicles.get(0).setHomeCity(estimatedHome);

		double distance;
		for(int i=1; i<opponentVehicles.size(); i++) {
			City randomCity;
			do {
				int randomId = random.nextInt(topology.cities().size());
				randomCity = topology.cities().get(randomId);
				distance = randomCity.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
			} while (distance < estimatedDistance);
			opponentVehicles.get(i).setHomeCity(randomCity);
		}
	}

	private double computeDistributionRatio(Task task, boolean us) {

		if (!useDistribution) {
			return 1.0;
		}

		// Proba that deliveryCity from extra task will be pickupCity from a future task
		double maxProbaFutureTask = distribution.probability(task.deliveryCity, null); 

		// Probas that extra task will be linked to a town we have already 
		List<Task> currentTasks = (us) ? new ArrayList<Task>(agent.getTasks()) : opponentTasks;
		for (Task currentTask: currentTasks) {
			maxProbaFutureTask =  Math.max(distribution.probability(currentTask.deliveryCity, task.pickupCity), maxProbaFutureTask);
			maxProbaFutureTask = Math.max(distribution.probability(task.deliveryCity, currentTask.pickupCity), maxProbaFutureTask);
		}

		// Constraint ratio to be between 0.9 and 1.1
		maxProbaFutureTask = 1.1 - maxProbaFutureTask/5;
		return maxProbaFutureTask; 
	}
	

	private double totalCost(List<Task> obtainedTasks, Task extraTask, boolean isUs, long startTime) {
		// Return the cost of the plan with the extra task

		List<Task> potentialTasks = new ArrayList<Task>(obtainedTasks); 
		potentialTasks.add(extraTask);
		
		double cost;
		if (isUs) {
			SLSAlgo algo = new SLSAlgo(vehicles, potentialTasks); 
			cost = algo.computeCostBestSolution(startTime+timeout_bid);
			this.newBestPlans = algo.getBestPlansEver();
		}
		else {
			SLSAlgo algo = new SLSAlgo(opponentVehicles, potentialTasks); 
			cost = algo.computeCostBestSolution(startTime+timeout_bid);
		}
		return cost;
	}

	// Make naive bid
	private Long naiveBid(Task task){
		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicles.get(0).costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;
		return (long) Math.round(bid); 
	}

	private List<Plan> computeNaivePlans(List<Vehicle> vehicles, TaskSet tasks) {
		// First vehicle get all the tasks, other vehicles can rest
        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }
        return plans;
	}

	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
		City current = vehicle.getCurrentCity();
		Plan plan = new Plan(current);

		for (Task task : tasks) {
			// move: current city => pickup location
			for (City city : current.pathTo(task.pickupCity))
				plan.appendMove(city);

			plan.appendPickup(task);

			// move: pickup location => delivery location
			for (City city : task.path())
				plan.appendMove(city);

			plan.appendDelivery(task);

			// set current city
			current = task.deliveryCity;
		}
		return plan;
	}
}
