package template;

import logist.topology.Topology.City;

public class State {
    public City fromCity;	
    private City toCity; //nullable if no task available
    
    public State(City fromCity, City toCity) {
        this.fromCity = fromCity;
        this.toCity = toCity;
    }

    public City getFromCity() {
        return this.fromCity;
    }

    public City getToCity() {
        return this.toCity;
    }

    public void setFromCity(City city) {
        this.fromCity = city;
    }

    public void setToCity(City city) {
        this.toCity = city;
    }

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof State))
			return false;
		State other = (State) obj;
		if (fromCity == null) {
			if (other.fromCity != null)
				return false;
		} else if (!(fromCity.id == other.fromCity.id))
			return false;
		if (toCity == null) {
			if (other.toCity != null)
				return false;
		} else if (!(toCity.id == other.toCity.id))
			return false;
		return true;
	} 
}