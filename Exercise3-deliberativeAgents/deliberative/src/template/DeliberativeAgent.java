package template;

/* import table */
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import template.AStarAlgo;
import template.BFSAlgo;


/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class DeliberativeAgent implements DeliberativeBehavior {

	// Different algorithms implemented 
	enum Algorithm { BFS, ASTAR, NAIVE};
	// Different heuristic implemented 
	enum Heuristic { NONE, SHORTEST };
	
	/* Environment */
	Topology topology;
	TaskDistribution td;
	
	/* the properties of the agent */
	Agent agent;
	int capacity;

	/* the planning class */
	Algorithm algorithm;
	Heuristic heuristic;
	
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		this.topology = topology;
		this.td = td;
		this.agent = agent;
		
		// initialize the planner
		int capacity = agent.vehicles().get(0).capacity();
		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
		String heuristicName = agent.readProperty("heuristic", String.class, "NONE");
		
		// Throws IllegalArgumentException if algorithm is unknown
		this.algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
		
		// Throws IllegalArgumentException if heuristic is unknown
		this.heuristic = Heuristic.valueOf(heuristicName.toUpperCase()); 
	}
	
	@Override
	public Plan plan(Vehicle vehicle, TaskSet tasks) {
		
		City currentCity = vehicle.getCurrentCity();
		Plan plan = new Plan(currentCity);

		long start;
		long end;

		// Compute the plan with the selected algorithm.
		switch (algorithm) {
		case ASTAR:
			// Agent plans its path through the A* algo
			AStarAlgo aStar = new AStarAlgo(this.heuristic, vehicle);
			start = System.currentTimeMillis();
			plan = aStar.computePlan(vehicle, tasks);
			end = System.currentTimeMillis();
			break;
		case BFS:
			// Agent plans its path through the BFS algo
			start = System.currentTimeMillis();
			BFSAlgo bfs = new BFSAlgo(vehicle);
			plan = bfs.computePlan(vehicle, tasks);
			end = System.currentTimeMillis();
			break;
		case NAIVE:
			// Agent plans its path through a naive approach
			start = System.currentTimeMillis();
			plan = naivePlan(vehicle, tasks);
			end = System.currentTimeMillis();
			break;
		default:
			throw new AssertionError("Should not happen.");
		}

		// Keep track of the time to compute the plan 
		System.out.println("Algorithm " + algorithm.toString()  + " took " + (end - start)/1000.0 + " seconds");	
		return plan;
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

	@Override
	public void planCancelled(TaskSet carriedTasks) {
		if (!carriedTasks.isEmpty()) {
			// This cannot happen for this simple agent, but typically
			// you will need to consider the carriedTasks when the next
			// plan is computed.
		}
	}
}
