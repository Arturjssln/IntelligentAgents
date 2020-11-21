package bestagent;

import java.awt.Color;

import logist.simulation.Vehicle;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class FashionVehicle implements Vehicle {

    private Vehicle vehicle;
    private City homeCity;

	public FashionVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
		this.homeCity = vehicle.homeCity();
	}

    @Override
	public City getCurrentCity() {
		return vehicle.getCurrentCity();
    }
    
    @Override
    public City homeCity() {
		return homeCity;
	}

	public void setHomeCity(City homeCity) {
		this.homeCity = homeCity;
	}

	@Override
	public int id() {
		return vehicle.id();
	}

	@Override
	public String name() {
		return vehicle.name();
	}

	@Override
	public int capacity() {
		return vehicle.capacity();
	}

	@Override
	public double speed() {
		return vehicle.speed();
	}

	@Override
	public int costPerKm() {
		return vehicle.costPerKm();
	}

	@Override
	public TaskSet getCurrentTasks() {
		return vehicle.getCurrentTasks();
	}

	@Override
	public long getReward() {
		return vehicle.getReward(); 
	}

	@Override
	public long getDistanceUnits() {
		return vehicle.getDistanceUnits();
	}

	@Override
	public double getDistance() {
		return vehicle.getDistance();
	}

	@Override
	public Color color() {
		return vehicle.color();
	}
}