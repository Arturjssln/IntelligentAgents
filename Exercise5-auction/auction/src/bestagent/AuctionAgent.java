package bestagent;

//the list of imports
import java.util.ArrayList;
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
public class AuctionAgent implements AuctionBehavior {
	// Different algorithms implemented
	enum Algorithm {AUCTION, NAIVE};
	
	private Algorithm algorithm;

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		String algorithmName = agent.readProperty("algorithm", String.class, "SLS");

		this.algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		if (winner == agent.id()) {
			currentCity = previous.deliveryCity;
		}
	}
	
	@Override
	public Long askPrice(Task task) {

		if (vehicle.capacity() < task.weight)
			return null;

		long distanceTask = task.pickupCity.distanceUnitsTo(task.deliveryCity);
		long distanceSum = distanceTask
				+ currentCity.distanceUnitsTo(task.pickupCity);
		double marginalCost = Measures.unitsToKM(distanceSum
				* vehicle.costPerKm());

		double ratio = 1.0 + (random.nextDouble() * 0.05 * task.id);
		double bid = ratio * marginalCost;

		return (long) Math.round(bid);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
		long time_start = System.currentTimeMillis();
        // Compute the plans for each vehicle with the selected algorithm.
        List<Plan> plans = new ArrayList<Plan>();
		switch (algorithm) {
            case NAIVE:
                /* A very simple auction agent that assigns all tasks to its first vehicle and handles them sequentially. */
                plans = computeNaivePlans(vehicles, tasks);
                break;
            case AUCTION:
				//do nothing for now
				
                break;
            default:
                throw new AssertionError("Should not happen.");
        }

        long time_end = System.currentTimeMillis();
        double duration = (time_end - time_start) / 1000.0;
        System.out.println("The plan was generated in " + duration + " seconds.");
        
        return plans;
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
