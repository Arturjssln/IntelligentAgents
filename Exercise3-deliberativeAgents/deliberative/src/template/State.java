package template;

import logist.topology.Topology.City;
import logist.task.TaskSet;
import logist.task.Task;
import logist.plan.Plan;

public class State {

    // Attributes
    private City currentCity;	
    public TaskSet pickedUpTasks;
	public TaskSet freeTasks;
	public Plan plan;
    
    // Constructor
    public State(City initialCity) {
		this.currentCity = initialCity;
		this.pickedUpTasks = new TaskSet(); 
		this.awaitingDeliveryTasks = new TaskSet(); 
		this.plan = new Plan(initialCity)
    }

    // Getters
    public City getCurrentCity() {
        return this.currentCity;
	}

	public TaskSet getpickedUpTasks() {
        return this.pickedUpTasks;
	}

	public TaskSet getAwaitingDeliveryTasks() {
        return this.awaitingDeliveryTasks;
	}
	
	public Plan getPlan() {
        return this.plan;
    }

    // Setters
    public void setCurrentCity(City city) {
        this.currentCity = city;
	}

	public void setpickedUpTasks(TaskSet tasks;) {
        this.pickedUpTasks = tasks;
    }

	public void setAwaitingDeliveryTasks(TaskSet tasks;) {
        this.awaitingDeliveryTasks = tasks;
	}
	
	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public Boolean isLastTask() {
		return (this.pickedUpTasks.isEmpty() && this.awaitingDeliveryTasks.isEmpty());
	}

	public State createSuccessor(Task taskToAdd) {

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
		if (fromCity == null) {
			if (other.getFromCity() != null)
				return false;
		} else if (!(fromCity.id == other.getFromCity().id))
			return false;
		if (toCity == null) {
			if (other.getToCity() != null)
				return false;
		} else if (!(toCity.id == other.getToCity().id))
			return false;
		return true;
    } 
    
    @Override
    // Redefine the hashCode method
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result += prime * result + ((fromCity == null) ? 0 : fromCity.hashCode());
		result += prime * result + ((toCity == null) ? 0 : toCity.hashCode());
		return result;
	}
}