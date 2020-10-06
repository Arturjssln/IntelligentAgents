package template;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveAgent implements ReactiveBehavior {

	static private final double EPSILON = 10e-4;

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private State currentState;
	
	private ArrayList<State> myStates = new ArrayList<State>();
	private HashMap<State, Double> bestValueForState = new HashMap<State, Double>();
	private HashMap<State, State> bestStateForState = new HashMap<State, State>();


	@Override
	public void setup(Topology cityTopology, TaskDistribution td, Agent agent) {

		// Reads the discount faccityTor cityFrom the agents.xml file.
		// If the property is not present it defaults cityTo 0.95
		Double discount = agent.readProperty("discount-faccityTor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.currentState = new State(null, null);
		
		learnValue(cityTopology, td);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		/*try {*/
		Action action;
		currentState.setFromCity(vehicle.getCurrentCity());
		if (availableTask != null) { //If there is an available task
			currentState = new State(vehicle.getCurrentCity(), availableTask.deliveryCity);
			// TODO: check is equals is the right condition
			boolean takeTask = currentState.equals(bestStateForState.get(currentState));
			if (takeTask) {
				System.out.println(vehicle.name() + " picked up a task cityFrom " + availableTask.pickupCity + " cityTo " + availableTask.deliveryCity);
				action = new Pickup(availableTask);
			} else {
				//TODO: merge this with next condition
				currentState.setToCity(null);
				currentState = bestStateForState.get(currentState);
				action = new Move(currentState.getToCity());
				System.out.println(vehicle.name() + " refused cityTo pick up a task cityFrom " + availableTask.pickupCity + " cityTo " + availableTask.deliveryCity + ", new destination is " + currentState.getToCity());
			}
		} else { //If there's no available task
			currentState = new State(vehicle.getCurrentCity(), null);
			System.out.println(currentState.getFromCity());
			System.out.println(currentState.getToCity());
			System.out.println(bestStateForState.containsKey(currentState));
			currentState = bestStateForState.get(currentState);
			action = new Move(currentState.getToCity());
			System.out.println(vehicle.name() + " found no task cityFrom " + availableTask.pickupCity + " cityTo " + availableTask.deliveryCity + ", new destination is " + currentState.getToCity());

		}
		if (numActions >= 1) {
			System.out.println("The cityTotal profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		return action;
		/*} catch (RuntimeException e) {
			System.out.println("ERROR2: " + e.toString());
		} catch (Exception e) {
			System.out.println("ERROR1: " + e.toString());
		}*/
	}

	
	private void learnValue(Topology cityTopology, TaskDistribution td) {
		double maxDiff;
		// Compute all possible states
		computeStates(cityTopology);
		//Initialize HashMap
		initializeValues();
		int iteration = 0;
		// Learning algorithm (Value iteration)
		do {
			maxDiff = -1e5;
			for (State state : myStates) {
				HashMap<City, Double> qValueForAction = new HashMap<City, Double>();

				List<City> actions = (state.getToCity() != null) ? cityTopology.cities() : state.getFromCity().neighbors();
				for (City action : actions) {
					double qValue = computeReward(state, action, td) + this.pPickup * computeTransitionProba(action, td);
					qValueForAction.put(action, qValue);
				}
				double maxQValue = Double.MIN_VALUE;
				City bestAction = null;
				for (City action : qValueForAction.keySet()) {
					double qValue_ = qValueForAction.get(action);
					maxQValue = qValue_ > maxQValue ? qValue_ : maxQValue;
					bestAction = qValue_ > maxQValue ? action : bestAction;
				}
				// Check convergence
				double diff = Math.abs(bestValueForState.get(state) - maxQValue);
				maxDiff = diff > maxDiff ? diff : maxDiff;

				// Update values
				bestValueForState.put(state, maxQValue);
				bestStateForState.put(state, new State(state.getFromCity(), bestAction));
			}
		iteration++;
		} while(maxDiff > EPSILON);
	}

	private void computeStates(Topology cityTopology) {
		List<City> cities = cityTopology.cities();
		for(City cityFrom : cities) {
			for(City cityTo : cities) {
				if (cityFrom.id != cityTo.id) {
					// Create all pickup possibilities
					myStates.add(new State(cityFrom, cityTo));
				}
			}
			// Create state when no task is available (only with neighbors)
			myStates.add(new State(cityFrom, null));
		}
	}

	private void initializeValues() {
		for(State state : myStates) {
			bestValueForState.put(state, random.nextDouble());
		}
	}

	private double computeTransitionProba(City action, TaskDistribution td) {
		double T = 0.0;
		for (State state_ : myStates) {
			City cityFrom = state_.getFromCity();
			City cityTo = state_.getToCity();
			if(cityFrom.equals(action)) {
				T += td.probability(cityFrom, cityTo) * bestValueForState.get(state_);
			}
		}
		return T;
	}

	private double computeReward(State state, City action, TaskDistribution td) {
		City cityFrom = state.getFromCity();
		Vehicle vehicle = myAgent.vehicles().get(0);
		double cost = vehicle.costPerKm() * cityFrom.distanceTo(action);
		double reward = (state.getToCity() != null) ? td.reward(cityFrom, action) : 0.0;
		return reward - cost;
	} 
}
