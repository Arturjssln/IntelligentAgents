package template;

import logist.topology.Topology.City;

public class State {

    // Attributes
    private City fromCity;	
    private City toCity; //nullable if no task available
    
    // Constructor
    public State(City fromCity, City toCity) {
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    // Getters
    public City getFromCity() {
        return this.fromCity;
    }

    public City getToCity() {
        return this.toCity;
    }


    // Setters
    public void setFromCity(City city) {
        this.fromCity = city;
    }

    public void setToCity(City city) {
        this.toCity = city;
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