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
	enum Strategy {HONNEST};
	
	private Algorithm algorithm;
	private BidStrategy strategy; 

	private long timeout_setup; 
    private long timeout_plan;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private List<Vehicle> vehicles;
	private City currentCity; 

	private double ourCost;
	private double opponentCost;
	private double ourNewCost;
	private double opponentNewCost;
	private List<Task> opponentTasks; 

	private int maxCapacity; 

	private List<Plan> newBestPlans; 
	private List<Plan> bestPlans; 


	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		 // This code is used to get the timeouts
		 LogistSettings ls = null;
		 try {
			 ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
		 }
		 catch (Exception exc) {
			 System.out.println("There was a problem loading the configuration file.");
		 }

		String algorithmName = agent.readProperty("algorithm", String.class, "SLS");
		String strategyName = agent.readProperty("strategy", String.class, "HONNEST");

		// Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		System.out.println("Algorithm used : " + algorithm.toString());
		Strategy strat = Strategy.valueOf(strategyName.toUpperCase());
        System.out.println("Strategy used : " + strat.toString());
        // The setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // The plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

		this.topology = topology;
		this.distribution = distribution; // TODO comprendre ce truc de distribution 
		this.agent = agent;
		this.vehicles = agent.vehicles();
		this.currentCity = vehicles.get(0).homeCity();
		initializeStrategy(strat); // TODO: choose the one we start with
		this.maxCapacity = initMaxCapacity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	public void initializeStrategy(Strategy strategyName) {
		switch (strategyName) {
			case HONNEST:
				this.strategy = new HonnestStrategy(); // TODO: choose the one we start with
				break;
			default: 
				throw new IllegalArgumentException("Should not happen.");

		}
	}	
		
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		
		if (winner == agent.id()) {
			this.bestPlans = this.newBestPlans; 
			this.ourCost = this.ourNewCost; //TODO: verify
		} 
		else {
			this.opponentTasks.add(previous);
			// TODO marginal cost de l'opponent 
			this.opponentCost = estimateOpponentCost(bids); //TODO 

			
		}

		//TODO: something with the distribution and with bids of opponents 

	}

	//TODO: mettre dans Strategy ? 
	private double estimateOpponentCost(Long[] bids) {
		return this.opponentNewCost;
	}
	

	private int initMaxCapacity(){
		int maxCapacity = 0; 
		for (Vehicle vehicle: this.vehicles) {
			maxCapacity = (vehicle.capacity() > maxCapacity) ? vehicle.capacity() : maxCapacity; 
		}
		return maxCapacity; 
	}
	
	@Override
	public Long askPrice(Task task) {

		if (maxCapacity < task.weight) { return null; }
		
		TaskSet currentTasks = agent.getTasks();
		List<Task> possibleTasks = new ArrayList<Task>(currentTasks);
		ourNewCost = totalCost(possibleTasks, task, true); 
		opponentNewCost = totalCost(opponentTasks, task, false); 

		return strategy.computeBid(ourNewCost - ourCost, opponentNewCost - opponentCost); 
	}

	private double totalCost(List<Task> obtainedTasks, Task extraTask, boolean isUs){
		List<Task> potentialTasks = new ArrayList<Task>(obtainedTasks); 
		potentialTasks.add(extraTask); 
		SLSAlgo algo = new SLSAlgo(vehicles, potentialTasks); // TODO : vehicles is for us
		double cost = algo.computeCostBestSolution(this.timeout_plan); //TODO: Change timeout
		if (isUs) {
			this.newBestPlans = algo.getBestPlansEver();
		}
		return cost;
	}


	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();
        // Compute the plans for each vehicle with the selected algorithm.
		List<Plan> plans = bestPlans;
		
        long time_end = System.currentTimeMillis();
        double duration = (time_end - time_start) / 1000.0;
        System.out.println("The plan was generated in " + duration + " seconds.");
        
        return plans;
	}

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
