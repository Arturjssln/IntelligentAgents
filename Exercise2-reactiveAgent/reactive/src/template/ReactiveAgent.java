package template;

import java.util.Arrays;
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
import template.State;

public class ReactiveAgent implements ReactiveBehavior {

	static private final double EPSILON = 10e-4;

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private State currentState;
	

	private HashMap<State, Double> bestValueInState = new HashMap<State, Double>();
	private HashMap<State, State> bestStateInState = new HashMap<State, State>();


	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class,
				0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		this.currentState = new State(agent.getCurrentCity(), null, false);
		
		learnValue(topology, td);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;

		if (availableTask != null) { //If there is an available task
			currentState = new State(currentState.getCurrentCity(), availableTask.deliveryCity, availableTask=true)
			state = bestStateInState()
			action = new Move()

		} else { //If there's no available task
			if (true) {
				System.out.println(vehicle.name() + " picks up a task from " + availableTask.pickupCity + " to " + availableTask.deliveryCity);
				action = new Pickup(availableTask);

			} else {
				City newCity = currentState.getCurrentCity().randomNeighbor(random);
				action = new Move(newCity);
				System.out.println(vehicle.name() + " refused to pick up a task from " + availableTask.pickupCity + " to " + availableTask.deliveryCity + ", new destination is " + newCity);
			}
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+numActions+" actions is "+myAgent.getTotalProfit()+" (average profit: "+(myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}

	
	private void learnValue(Topology topology, TaskDistribution td) {
		double maxDiff;
		double maxQValue;
		int nbCities = topology.size();
		double[] oldValues = new double[nbCities];
		double[] qValue = new double[nbCities];
		for (int i=0; i<values.length;i++) {
			values[i] = (new Random()).nextInt(10);
		}
		do {
			maxDiff = Double.MIN_VALUE;
			oldValues = values;
			for (int s=0; s<nbCities; s++) {
				maxQValue = Double.MIN_VALUE;
				Arrays.fill(qValue, Double.MIN_VALUE);
				City from = topology.cities().get(s);
				for (int a=0; a<nbCities; a++) {
					double sumProba = 0;
					City to = topology.cities().get(a);
					if (s==a) {continue;}
					for (int i=0; i<nbCities; i++) {
						sumProba += td.probability(from, to) * oldValues[i];
					}
					qValue[a] = td.reward(from, to) + this.pPickup * sumProba;
				}
				City bestCity = from;
				for(int i=0; i<nbCities; i++) {
					maxQValue = qValue[i] > maxQValue ? qValue[i] : maxQValue;
					bestCity = qValue[i] > maxQValue ? topology.cities().get(i) : bestCity;
				}
				this.bestValueInState.put(from, maxQValue);
				this.bestStateInState.put(from, bestCity);
			}
			for(int i=0; i<nbCities; i++) {
				double diff = Math.abs(oldValues[i] - values[i]);
				maxDiff = diff > maxDiff ? diff : maxDiff;
			}
		} while(maxDiff > EPSILON);
	}
}
