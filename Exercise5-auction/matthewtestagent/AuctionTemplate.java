package matthewtestagent;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import logist.Measures;
import logist.behavior.AuctionBehavior;
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
public class AuctionTemplate implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	
	private double ourCost;
	private double ourNewCost;
	private double oppCost;
	private double oppNewCost;
	
	private double ourReward;
	private double oppReward;

	private List<Task> allTasks;
	private List<Task> opponentsTasks;
	
	private CustomVehicle biggestVehicle;
	private int biggestVehicleCapacity = Integer.MIN_VALUE;
	
	private List<CustomVehicle> ourVehicles;
	private List<CustomVehicle> opponentsVehicles;
	
	final static double riskyRounds = 5;
	final static double totalRounds = 17;
	private double round = 0;
	private double tasksWon = 0;

	final static double riskyRate = 0.65;
	final static double initialRate = 0.85;

	private double oppRatio = 0.8;
	private double ourRatio = 0.65;

	private double ourMax = riskyRate;
	private double ourMin = riskyRate;

	private double bidOppMin = Double.MAX_VALUE;
	
	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;

		long seed = -9019554669489983951L * distribution.hashCode() * agent.id();
		this.random = new Random(seed);
		
		this.allTasks = new ArrayList<Task>();
		this.opponentsTasks = new ArrayList<Task>();
		this.ourVehicles = new ArrayList<CustomVehicle>();
		this.opponentsVehicles = new ArrayList<CustomVehicle>(); // naively assume same vehicles as ours
		for(Vehicle v: agent.vehicles()) {
			CustomVehicle custVehicle = new CustomVehicle(v);
			ourVehicles.add(custVehicle);
			opponentsVehicles.add(custVehicle);
			if(custVehicle.getCapacity() > biggestVehicleCapacity) {
				this.biggestVehicleCapacity = custVehicle.getCapacity();
				this.biggestVehicle = custVehicle;
			}
		}
	}
	
	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if(agent.id() != winner) {
			opponentsTasks.add(previous);
		}
		
		long ourBid = bids[agent.id()];
		long oppBid = bids[1 - agent.id()]; // works because id's range from 0 to 1.
		if (oppBid < bidOppMin) {
			bidOppMin = oppBid;
		}

		double trueOppRatio = (oppNewCost - oppCost);
		double epsilon = 0.1 + (0.01 - 0.1)*Math.exp(-((totalRounds - round)/(round)));

		//Every round upper and lower bound of the ratio are set to reflect a specific strategy
		if(round < riskyRounds){
			//In the beginning bids are set to be realy low. This risky behaviour is meant to capture at least a few tasks in the beginning to profit from cost-synergies between packages later on.
			ourMin = riskyRate + (initialRate - riskyRate)*Math.exp((round -riskyRounds)/round);
			ourMax = riskyRate;
		}
		else if (round < totalRounds) {
			//In between 'riskyround' and 'totalround' lost profit from risky bids are gradually won back. (Total amount of rounds is estimated to be around 17)
			double prof = (ourCost - ourReward)/((totalRounds - round)*0.65);
			double avgMC = ourCost/tasksWon;
			double profRatio = (avgMC + prof)/avgMC;
			ourMin = profRatio*(1 - 0.25*((totalRounds - round)/(totalRounds - riskyRounds)));
			ourMax = Double.MAX_VALUE;
		}
		else{
			//After estimated amount of rounds missed profits should be retreived in one round time. If their is excess profit than it is allowed bid low.
			double avgMC = ourCost/tasksWon;
			ourMin = (avgMC + (ourCost - ourReward))/avgMC;
			ourMax = Double.MAX_VALUE;
		}
		
		
		if (winner == agent.id()) {
			//When a task is won, oppRatio is changed to better reflect opp bid and myRatio is slightly upped
			ourCost = ourNewCost;
			ourReward += ourBid;
			tasksWon++;
//			myPlan.updatePlan(); // to verify

			ourRatio = Math.min(ourMax, ourRatio + epsilon);
			oppRatio += ((oppBid/(oppNewCost - oppCost)) - oppRatio)*0.25;

		} else {
			//Task lost, oppRatio is changed to better reflect oppBid and myRatio is slightly lowered
			oppCost = oppNewCost;
			oppReward += oppBid;
//			oppPlan.updatePlan(); // to verify

			ourRatio = Math.max(ourMin, ourRatio - epsilon);
			oppRatio += ((oppBid/(oppNewCost - oppCost)) - oppRatio)*0.5;
		}

		//Code to reassign one of the opponents vehicles to another city
		if (round == 1) {
			infereOpponentsInitialCity(previous, oppBid);
		}
	}
	
	@Override
	public Long askPrice(Task task) {
		if (biggestVehicleCapacity < task.weight)
			//Package not compatible with fleet
			return null;

		ourNewCost = ourMarginalCost(task);
		oppNewCost = opponentsMarginalCost(task);

		double myMarginalCost = ourNewCost - ourCost;
		double oppMarginalCost = oppNewCost - oppCost;

		System.out.println("Round: " + round);
		System.out.println("Predict cost:" + oppMarginalCost);

		double ourBid = 0.0;
		if (round < riskyRounds) {
			//During riskyrounds stick to own ratio to do riskier bids
			ourBid = ourRatio*myMarginalCost;
		}
		else{
			//Take maximum of predicted oppBid, minimum bid opp or own bid
			ourBid = Math.max(Math.max(oppMarginalCost * oppRatio, ourRatio * myMarginalCost), bidOppMin - 1);
		}
		
		round++;

		return (long) Math.floor(ourBid);
	}
	
	public double ourMarginalCost(Task task) {
		TaskSet previousTasks = agent.getTasks();
		List<Task> tempOppTasks = new ArrayList<Task>(previousTasks);
		tempOppTasks.add(task);
		
		CSP csp = new CSP(ourVehicles, tempOppTasks, 0.95, 1000);
		CSPSolution bestSolution = csp.calculateCSP();
		
		double solutionCost = csp.calculateTotalCost(bestSolution);
		
		return solutionCost;
	}
	
	public double opponentsMarginalCost(Task task) {
		List<Task> tempOppTasks = new ArrayList<Task>(opponentsTasks);
		tempOppTasks.add(task);
		
		CSP csp = new CSP(opponentsVehicles, tempOppTasks, 0.95, 1000);
		CSPSolution bestOpponentSolution = csp.calculateCSP();
		
		double solutionCost = csp.calculateTotalCost(bestOpponentSolution);
		
		return solutionCost;
	}
	
	public void infereOpponentsInitialCity(Task task, Long opponentBid) {
		List<City> ourCities = new ArrayList<City>();
		int lowestOpponentsCost = Integer.MAX_VALUE;
		for(Vehicle v: agent.vehicles()) {
			ourCities.add(v.homeCity());
			int vehicleCost = v.costPerKm();
			if(vehicleCost < lowestOpponentsCost) {
				lowestOpponentsCost = vehicleCost;
			}
		}
		
		double lowestCost = Double.MAX_VALUE;
		City opponentsInitialCity = null;
		
		for(City city: topology.cities()) {
			if(!ourCities.contains(city)) {
				double distance = city.distanceTo(task.pickupCity) + task.pickupCity.distanceTo(task.deliveryCity);
				double cost = Math.abs(opponentBid - (distance * lowestOpponentsCost));
				
				if(cost < lowestCost) {
					lowestCost = cost;
					opponentsInitialCity = city;
				}
			}
		}
//		assigns opponentsInitialCity to first vehicle of opponent and then randomly assigns some unassigned cities to rest of vehicles
		ourCities.add(opponentsInitialCity);
		for(CustomVehicle v: opponentsVehicles) {
			Random random = new Random();
			City randomCity;
			do {
				int randomNum = random.nextInt(topology.cities().size());
				randomCity = topology.cities().get(randomNum);
			} while (ourCities.contains(randomCity));
			ourCities.add(randomCity);
			v.setInitCity(randomCity);
		}
		opponentsVehicles.get(0).setInitCity(opponentsInitialCity);
	}
	
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        List<Plan> plans = centralizedPlan(vehicles, tasks);
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in "+duration+" milliseconds.");
        
        return plans;
    }
    
    private List<Plan> centralizedPlan(List<Vehicle> vehicles, TaskSet tasks) { 
    		List<CustomVehicle> customVehicles = new ArrayList<CustomVehicle>(); 
    		for(Vehicle v: vehicles) {
    			CustomVehicle custVehicle = new CustomVehicle(v);
    			customVehicles.add(custVehicle);
    		}
    		List<Task> currentTasks = new ArrayList<Task>(tasks);
    		CSP csp = new CSP(customVehicles, currentTasks, 0.95, 1000);
    		List <Plan> plans = csp.createCentralizedPlan();
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
