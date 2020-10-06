package template;

import logist.topology.Topology.City;

public class State {
    private City fromCity;	
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
        this.fromCity = fromCity;
    }

    public void setToCity(City city) {
        this.toCity = toCity;
    }

    @Override
	public boolean equals(Object obj) {
        // TODO enlever copié collé
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (fromCity == null) {
			if (other.getFromCity() != null)
				return false;
		} else if (!fromCity.equals(other.getFromCity()))
			return false;
		if (toCity == null) {
			if (other.getToCity() != null)
				return false;
		} else if (!toCity.equals(other.getToCity()))
			return false;
		return true;
	} 


}