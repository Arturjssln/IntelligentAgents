package template;

import logist.topology.Topology.City;

public class State {
    private boolean taskAvailable;
    private City fromCity;	
    private City toCity;
    
    public State(City fromCity, City toCity, boolean taskAvailable) {
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.taskAvailable = taskAvailable;
    }

    public boolean getTaskAvailable(City city) {
        return this.taskAvailable;
    }

    public City getFromCity(City city) {
        return this.fromCity;
    }

    public City getToCity(City city) {
        return this.toCity;
    }
    

    public void setTaskAvailable(boolean taskAvailable) {
        this.taskAvailable = taskAvailable;
    }

    public void setFromCity(City city) {
        this.fromCity = fromCity;
    }

    public void setToCity(City city) {
        this.toCity = toCity;
    }



}