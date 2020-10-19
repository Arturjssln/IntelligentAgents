package template;

import logist.topology.Topology.City;
import logist.task.TaskSet;
import logist.plan.Plan;

public class State {

    // Attributes
    private City currentCity;	
    public TaskSet pickedUpTasks;
	public TaskSet awaitingDeliveryTasks;
	public Plan plan;
	private double cost; 
    
    // Constructor
    public State(City initialCity) {
		this.currentCity = initialCity;
		this.pickedUpTasks = null;
		this.awaitingDeliveryTasks = null;
		this.plan = new Plan(initialCity); 
	}
	
	// Copy constructor
    public State(State state) { 
        this.currentCity = state.getCurrentCity(); 
        this.pickedUpTasks = state.pickedUpTasks;
		this.awaitingDeliveryTasks = state.awaitingDeliveryTasks;
		this.plan = state.plan; // TODO: Vérifier que ça ça marche
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

	public double getCost() {
		return this.cost; 
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

	public void setCost(double cost) {
		this.cost = cost; 
	}

	public Boolean isLastTask() {
		return (this.pickedUpTasks.isEmpty() && this.awaitingDeliveryTasks.isEmpty());
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
		if (currentCity == null) {
			if (other.getCurrentCity() != null)
				return false;
		} else if (!(currentCity.id == other.getCurrentCity().id))
			return false;
		// TODO: TO CONTINUE
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
		return result;
	}
}