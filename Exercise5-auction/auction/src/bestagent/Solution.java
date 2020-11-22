package bestagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logist.plan.Plan;
import logist.task.Task;
import logist.topology.Topology.City;

public class Solution {

    // To solve discrete constraint optimization problem (COP)

    /**
     * The Solution class aims at implemented the possible set of actions
     * that could describe a solution to the COP
     * nextTaskForVehicle: maps a vehicle to its first task
     * nextTaskForTask: maps a task to its next task 
     * pickupTimes: maps a task to its pickup time step
     * deliveryTimes: maps a task to its delivery time step
     * vehicles: maps a task to the vehicle handling it
     */

    public HashMap<Integer, Task> nextTaskForVehicle;
    public HashMap<Task, Task> nextTaskForTask;
    public HashMap<Task, Integer> pickupTimes;
    public HashMap<Task, Integer> deliveryTimes;
    public HashMap<Task, Integer> vehicles;
    public List<Plan> plans;

    // useful private attribute to validate the constraints 
    private ArrayList<ArrayList<Task>> tasksAtTimeStepForVehicles;

    // Constructor 
    public Solution() {
        this.nextTaskForTask = new HashMap<Task, Task>();
        this.nextTaskForVehicle = new HashMap<Integer, Task>();
        this.pickupTimes = new HashMap<Task, Integer>();
        this.deliveryTimes = new HashMap<Task, Integer>();
        this.vehicles = new HashMap<Task, Integer>();
        this.plans = new ArrayList<Plan>(); 
    }

    // Copy constructor
	public Solution(Solution solution) {
        this.nextTaskForTask = new HashMap<Task, Task>(solution.nextTaskForTask);
        this.nextTaskForVehicle = new HashMap<Integer, Task>(solution.nextTaskForVehicle);
        this.pickupTimes = new HashMap<Task, Integer>(solution.pickupTimes);
        this.deliveryTimes = new HashMap<Task, Integer>(solution.deliveryTimes);
        this.vehicles = new HashMap<Task, Integer>(solution.vehicles);
        this.plans = new ArrayList<Plan>(solution.plans);
    }

    // Generate company plan based on the attributes of the solution 
    public void computePlans(List<FashionVehicle> vehicles) {
        plans = new ArrayList<Plan>();
        
        int nbTasksPickedUp = 0;
        int nbTasksDelivered = 0;
        // Create a plan for each vehicle
        for (FashionVehicle vehicle : vehicles) {
            City currentCity = vehicle.homeCity();
            Plan plan = new Plan(currentCity);
            
            // Set first task
            Task currentTask = nextTaskForVehicle.get(vehicle.id());
            if (currentTask != null) {
                for (City city : currentCity.pathTo(currentTask.pickupCity)) {
                    plan.appendMove(city);
                }
                currentCity = currentTask.pickupCity;
                plan.appendPickup(currentTask);
                nbTasksPickedUp++;
                Task taskToDeliver = null;
                int currentTimeStep = 1;

                while(nextTaskForTask.get(currentTask) != null) {
                    Task nextTask = nextTaskForTask.get(currentTask);
                    int pickupTimeNextTask = pickupTimes.get(nextTask);
                    
                    while(pickupTimeNextTask > currentTimeStep) {
                        taskToDeliver = null;
                        for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                            if (set.getValue().equals(currentTimeStep) && (this.vehicles.get(set.getKey()) == vehicle.id())) {
                                taskToDeliver = set.getKey();
                            }
                        }
                        if (taskToDeliver != null) {
                            for (City city : currentCity.pathTo(taskToDeliver.deliveryCity)) {
                                plan.appendMove(city);
                            }
                            currentCity = taskToDeliver.deliveryCity;
                            plan.appendDelivery(taskToDeliver);
                            nbTasksDelivered++;
                            currentTask = taskToDeliver;
                        }
                        currentTimeStep++;
                    }

                    for (City city : currentCity.pathTo(nextTask.pickupCity)) {
                        plan.appendMove(city);
                    }
                    currentCity = nextTask.pickupCity;
                    plan.appendPickup(nextTask);
                    nbTasksPickedUp++;
                    currentTask = nextTask;
                    currentTimeStep++;
                }
                do {
                    taskToDeliver = null;
                    for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                        if (set.getValue().equals(currentTimeStep) && (this.vehicles.get(set.getKey()) == vehicle.id())) {
                            taskToDeliver = set.getKey();
                        }
                    }

                    if (taskToDeliver != null) {
                        for (City city : currentCity.pathTo(taskToDeliver.deliveryCity)) {
                            plan.appendMove(city);
                        }
                        currentCity = taskToDeliver.deliveryCity;
                        plan.appendDelivery(taskToDeliver);
                        nbTasksDelivered++;
                    }
                    currentTimeStep++;
                    currentTask = taskToDeliver;
                } while(nbTasksDelivered != nbTasksPickedUp);
            }
            plans.add(plan);
        }
    }

    
    // Check that this instance satisfies the constraints
    public boolean isValid(List<Task> tasks, List<FashionVehicle> vehicles) {   
    	
        // All tasks must be delivered
        if (!attributeSizeValid(tasks, vehicles)) { return false; }
        for (Map.Entry<Task, Task> set : nextTaskForTask.entrySet()) {
            // Task delivered after some task t cannot be the same task
            if (set.getKey() == set.getValue()) { return false; }
            if (set.getValue() != null) {
                // Task handled after task t must be picked up after task t
                if (pickupTimes.get(set.getValue()) <= pickupTimes.get(set.getKey())) { return false;}
            }
            // Task delivered after some task t is handled with the same vehicle
            if ((set.getValue() != null) && (this.vehicles.get(set.getValue()) != this.vehicles.get(set.getKey()))) { return false;}
        }
        for (Map.Entry<Integer, Task> set : nextTaskForVehicle.entrySet()) {
            if (set.getValue() != null) {
                // The first task of a vehicle must be picked up at time step 0
                if (pickupTimes.get(set.getValue()) != 0) { return false;}
                // The first task of a vehicle must be delivered by this vehicle
                if (this.vehicles.get(set.getValue()) != set.getKey()) { return false;}
            }
        }

        for (Map.Entry<Task, Integer> set : pickupTimes.entrySet()) {
            // The delivery time step of a task must be higher than its pickup time step
            if (deliveryTimes.get(set.getKey()) <= set.getValue()) { return false; }
        }
        
        int timeStep = 1;
        Double[] pickedUpTasksWeight = new Double[vehicles.size()]; 
        for (int i = 0; i < vehicles.size(); i++) {
            pickedUpTasksWeight[i] = (nextTaskForVehicle.get(i) != null) ? nextTaskForVehicle.get(i).weight : 0.0;

        }
        int timeBeforeStopping = 0;
        while(updateTasksAtTimeStepForVehicles(timeStep, vehicles) || (timeBeforeStopping < 10)) {
        	timeBeforeStopping++;
            for (int i=0; i<vehicles.size(); i++) {
                List<Task> tasksAtTimeStep = tasksAtTimeStepForVehicles.get(i);
                // Only one action per vehicle can happen
                if (tasksAtTimeStep.size() > 1) { return false;}
                if (tasksAtTimeStep.size() == 1) {
                    Task task = tasksAtTimeStep.get(0);
                    if (pickupTimes.get(task) == timeStep) {
                        pickedUpTasksWeight[i] += task.weight;
                    } else {
                        pickedUpTasksWeight[i] -= task.weight;
                    }
                    // A vehicle capacity cannot be exceeded
                    if (pickedUpTasksWeight[i] > vehicles.get(i).capacity()) { return false;}
                    timeBeforeStopping = 0;
                }
            }
            timeStep++;
        }
        return true; 
    }

    // Returns all the tasks performed by a given vehicle at a given timetep 
    private boolean updateTasksAtTimeStepForVehicles(Integer timeStep, List<FashionVehicle> vehicles) {
        List<Task> tasksAtTime = new ArrayList<Task>(); 
        tasksAtTime.addAll(getTasksAtTime(pickupTimes, timeStep));
        tasksAtTime.addAll(getTasksAtTime(deliveryTimes, timeStep));

        ArrayList<ArrayList<Task>> tasksAtTimeStepForVehicles = new ArrayList<>(vehicles.size());
        for(int i=0; i < vehicles.size(); i++) {
        	tasksAtTimeStepForVehicles.add(new ArrayList<Task>());
        }
        for (Task task : tasksAtTime) {
        	tasksAtTimeStepForVehicles.get(this.vehicles.get(task)).add(task);
        }
        this.tasksAtTimeStepForVehicles = tasksAtTimeStepForVehicles;
        return (!tasksAtTime.isEmpty()); 
    }

    // Check the validity of the attribute size 
    private boolean attributeSizeValid(List<Task> tasks, List<FashionVehicle> vehicles) {
        return ((vehicles.size() >= nextTaskForVehicle.size()) &&
                (tasks.size() == nextTaskForTask.size()) && 
                (tasks.size() == pickupTimes.size()) &&
                (tasks.size() == deliveryTimes.size()) && 
                (tasks.size() == this.vehicles.size()));
    }

    // Returns all the tasks performed by all vehicles at time timeStep
    private List<Task> getTasksAtTime(HashMap<Task, Integer> times, Integer timeStep) {
        List<Task> tasksAtTime = new ArrayList<Task>(); 
        for (Map.Entry<Task, Integer> set : times.entrySet()) {
            if (set.getValue().equals(timeStep)) {
                tasksAtTime.add(set.getKey());
            }
        }
        return tasksAtTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Solution that = (Solution) obj;
        return (this.nextTaskForTask == that.nextTaskForTask
                    || (this.nextTaskForTask != null && this.nextTaskForTask.equals(that.nextTaskForTask)))
                && (this.nextTaskForVehicle == that.nextTaskForVehicle 
                    || (this.nextTaskForVehicle != null && this.nextTaskForVehicle.equals(that.nextTaskForVehicle)))
                && (this.pickupTimes == that.pickupTimes 
                    || (this.pickupTimes != null && this.pickupTimes.equals(that.pickupTimes)))
                && (this.deliveryTimes == that.deliveryTimes 
                    || (this.deliveryTimes != null && this.deliveryTimes.equals(that.deliveryTimes)))
                && (this.vehicles == that.vehicles 
                    || (this.vehicles != null && this.vehicles.equals(that.vehicles)))
                && (this.plans == that.plans 
                    || (this.plans != null && this.plans.equals(that.plans)));
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((nextTaskForVehicle == null) ? 0 : nextTaskForVehicle.hashCode());
        result = prime * result
                + ((nextTaskForTask == null) ? 0 : nextTaskForTask.hashCode());
        result = prime * result
                + ((deliveryTimes == null) ? 0 : deliveryTimes.hashCode());
        result = prime * result
                + ((pickupTimes == null) ? 0 : pickupTimes.hashCode());
        result = prime * result
                + ((vehicles == null) ? 0 : vehicles.hashCode());
        result = prime * result
                + ((plans == null) ? 0 : plans.hashCode());
        return result;
    }

}