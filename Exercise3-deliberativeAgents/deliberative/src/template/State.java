package template;

import logist.topology.Topology.City;
import template.DeliberativeAgent.Heuristic;
import logist.task.Task;
import logist.task.TaskSet;
import logist.plan.Action;
import logist.plan.Plan;

public class State {

	/* The class is defined by 6 attributes
	currentCity: the city in which the agent is
	pickedUpTasks: the tasks that the agent has in its vehicle, waiting to be delivered
	awaitingDeliveryTasks: the tasks in the environnment, waiting to be picked up
	plan: list of actions needed to get from initial city to the currentCity
	cost: cost to pay to get from initial city to the currentCity 
	heuristic: value of the heuristic at the stage of the state
	*/

    // Attributes
    private City currentCity;	
    public TaskSet pickedUpTasks;
	public TaskSet awaitingDeliveryTasks;
	public Plan plan;
	private double cost; 
	private double heuristic;
    
    // Constructor
    public State(City initialCity, TaskSet pickedUpTasks, TaskSet awaitingDeliveryTasks) {
		this.currentCity = initialCity;
		this.pickedUpTasks = pickedUpTasks;
		this.awaitingDeliveryTasks = awaitingDeliveryTasks;
		this.plan = new Plan(initialCity); 
	}
	
	// Copy constructor
    public State(State state) { 
        this.currentCity = state.getCurrentCity(); 
        this.pickedUpTasks = state.pickedUpTasks.clone();
		this.awaitingDeliveryTasks = state.awaitingDeliveryTasks.clone();
		// Deep copy 
		this.plan = new Plan(this.currentCity);
		for (Action action : state.plan) {
			this.plan.append(action);
		}
	} 
	
	// Methods

	public void computeCost(double costPerKm) {
		// Compute the cost from the total distance travelled and the cost per km
		this.cost = plan.totalDistance() * costPerKm; 
	}

	public void computeHeuristic(Heuristic heuristic, double costPerKm) {
		// Compute the wanted heuristic 
		switch (heuristic) {
			case NONE:
				this.heuristic = 0.0;
				break;
			case LONGESTTASK: 
				this.heuristic = computeHeuristicLongestTask() * costPerKm;
			default:
				break;
		}
	}

	private double computeHeuristicLongestTask() {
		/* Compute the longest travelled distance to pickup and deliver 
		(or only deliver if already picked up) a task */
		double heuristic = 0.0; 

		// Browse distances to travel to pick up and deliver awaiting tasks
		for (Task awaitingDeliveryTask : awaitingDeliveryTasks) {
			heuristic = Math.max(heuristic, (this.currentCity.distanceTo(awaitingDeliveryTask.pickupCity)
							+ awaitingDeliveryTask.pickupCity.distanceTo(awaitingDeliveryTask.deliveryCity)));
		}
		// Browse distances to travel to deliver picked up tasks
		for (Task pickedUpTask : pickedUpTasks) {
			heuristic = Math.max(heuristic, this.currentCity.distanceTo(pickedUpTask.deliveryCity));
		}

		return heuristic;
	}

	public Boolean isGoalState() {
		// Check if all the tasks have been picked up and delivered: goal state 
		return (this.pickedUpTasks.isEmpty() && this.awaitingDeliveryTasks.isEmpty());
	}

    // Getters
    public City getCurrentCity() {
        return this.currentCity;
	}

	public TaskSet getPickedUpTasks() {
        return this.pickedUpTasks;
	}

	public TaskSet getAwaitingDeliveryTasks() {
        return this.awaitingDeliveryTasks;
	}
	
	public Plan getPlan() {
        return this.plan;
	}
	
	public double getTotalCost() {
		return cost + heuristic;
	}

	public double getCost() {
		return cost;
	}

	public double getHeuristic() {
		return heuristic;
	}

    // Setters
    public void setCurrentCity(City city) {
        this.currentCity = city;
	}

	public void setPickedUpTasks(TaskSet tasks) {
        this.pickedUpTasks = tasks;
    }

	public void setAwaitingDeliveryTasks(TaskSet tasks) {
        this.awaitingDeliveryTasks = tasks;
	}
	
	public void setPlan(Plan plan) {
		this.plan = plan;
	}

    @Override
    // Redefine the equals method
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof State))
			return false;
		State other = (State) obj;
		// Check current city
		if (currentCity == null) {
			if (other.getCurrentCity() != null)
				return false;
		} else if (!(currentCity.id == other.getCurrentCity().id))
			return false;
		// Check picked up tasks
		if (pickedUpTasks == null) {
			if (other.pickedUpTasks != null)
				return false;
		} else if (!(pickedUpTasks.equals(other.pickedUpTasks)))
			return false;
		// Check awaiting delivery tasks
		if (awaitingDeliveryTasks == null) {
			if (other.awaitingDeliveryTasks != null)
				return false;
		} else if (!(awaitingDeliveryTasks.equals(other.awaitingDeliveryTasks)))
			return false;
		// Check plan
		if (plan == null) {
			if (other.plan != null)
				return false;
		} else if (!(plan.equals(other.plan)))
			return false;
		// Check cost
		if (!(cost == other.getCost()))
			return false;
		// Check heuristic
		if (!(heuristic == other.getHeuristic()))
			return false;
		return true;
	} 
    
    @Override
    // Redefine the hashCode method
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result += prime * result + ((currentCity == null) ? 0 : currentCity.hashCode());
		result += prime * result + ((pickedUpTasks == null) ? 0 : pickedUpTasks.hashCode());
		result += prime * result + ((awaitingDeliveryTasks == null) ? 0 : awaitingDeliveryTasks.hashCode());
		result += prime * result + ((plan == null) ? 0 : plan.hashCode());
		return result;
	}
}