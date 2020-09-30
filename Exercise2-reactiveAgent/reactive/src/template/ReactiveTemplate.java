package template;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import apple.laf.JRSUIState.ValueState;
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

public class ReactiveTemplate implements ReactiveBehavior {

	static private final double EPSILON = 10e-4;

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private double[] values;
	

	private HashMap<City, Double> bestValueInState = new HashMap<City, Double>();
	private HashMap<City, City> bestStateInState = new HashMap<City, City>();


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
		
		learnValue(topology, td);

	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		Action action;
		
		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
		} else {
			action = new Pickup(availableTask);
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
		double[] values = new double[nbCities];
		for (double val : values) {
			val = (new Random()).nextInt(10);
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
