package template;

import logist.topology.Topology.City;

public class State {


    // Attributes
    private City currentCity;	
    private TaskSet pickedUpTasks; // delivered tasks 
    private TaskSet freeTasks; // task to be delivered or going to be pick up in the state
    
    // Constructor
    public State(City currentCity) {
        this.currentCity = currentCity;
        this.currentTasks = new List<TaskStatut>(); 
    }

    // Getters
    public City getCurrentCity() {
        return this.currentCity;
    }

    public List<TaskStatut> getcurrentTasks(){
        return currentTasks; 
    }


    // Setters
    public void setCurrentCity(City city) {
        this.currentCity = city;
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