package template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskSet;
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

    // useful private attribute to validate the constraits 
    private ArrayList<ArrayList<Task>> tasksAtTimeStepForVehicles;

    // Constructor 
    public Solution() {
        this.nextTaskForTask = new HashMap<Task, Task>();
        this.nextTaskForVehicle = new HashMap<Integer, Task>();
        this.pickupTimes = new HashMap<Task, Integer>();
        this.deliveryTimes = new HashMap<Task, Integer>();
        this.vehicles = new HashMap<Task, Integer>();
    }

    // Copy constructor
	public Solution(Solution solution) {
        this.nextTaskForTask = new HashMap<Task, Task>(solution.nextTaskForTask);
        this.nextTaskForVehicle = new HashMap<Integer, Task>(solution.nextTaskForVehicle);
        this.pickupTimes = new HashMap<Task, Integer>(solution.pickupTimes);
        this.deliveryTimes = new HashMap<Task, Integer>(solution.deliveryTimes);
        this.vehicles = new HashMap<Task, Integer>(solution.vehicles);
    }

    // Generate company plan based on the attributes of the solution 
    public List<Plan> generatePlans(List<Vehicle> vehicles) {
        
        List<Plan> plans = new ArrayList<Plan>();
        //System.out.println("Sizes: " + nextTaskForTask.size() + " " + nextTaskForVehicle.size() + " " +  pickupTimes.size() + " " + deliveryTimes.size() + " " + vehicles.size());
        
        // Create a plan for each vehicle
        for (Vehicle vehicle : vehicles) {
            City currentCity = vehicle.getCurrentCity();
            Plan plan = new Plan(currentCity);
            
            // Set first task
            Task currentTask = nextTaskForVehicle.get(vehicle.id());
            if (currentTask != null) {
                //System.out.println("First task : " + currentTask.toString());  
                for (City city : currentCity.pathTo(currentTask.pickupCity)) {
                    plan.appendMove(city);
                    //System.out.println("Move0 --> " + city.toString());
                }
                currentCity = currentTask.pickupCity;
                plan.appendPickup(currentTask);
                Task taskToDeliver = null;
                int currentTimeStep = 1;
                //System.out.println("Picked up :  " + currentTask.toString() + " (picked up at 0)");

                while(nextTaskForTask.get(currentTask) != null) {
                    Task nextTask = nextTaskForTask.get(currentTask);
                    int pickupTimeNextTask = pickupTimes.get(nextTask);
                    //System.out.println("Current timestep : " + currentTimeStep);
                    //System.out.println("Next task :  " + nextTask.toString() + " will be picked up at " + pickupTimeNextTask);
                    
                    while(pickupTimeNextTask > currentTimeStep) {
                        for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                            if (set.getValue().equals(currentTimeStep) && (this.vehicles.get(set.getKey()) == vehicle.id())) {
                                taskToDeliver = set.getKey();
                            }
                        }
                        //System.out.println(currentTask.deliveryCity.toString() + " --> " + taskToDeliver.deliveryCity.toString());
                        for (City city : currentCity.pathTo(taskToDeliver.deliveryCity)) {
                            plan.appendMove(city);
                            //System.out.println("Move1 --> " + city.toString());
                        }
                        currentCity = taskToDeliver.deliveryCity;
                        plan.appendDelivery(taskToDeliver);
                        //System.out.println("Delivered :  " + taskToDeliver.toString() + " ( delivered at " + currentTimeStep + ")");
                        currentTask = taskToDeliver;
                        currentTimeStep++;
                    }

                    for (City city : currentCity.pathTo(nextTask.pickupCity)) {
                        plan.appendMove(city);
                        //System.out.println("Move2 --> " + city.toString());
                    }
                    currentCity = nextTask.pickupCity;
                    plan.appendPickup(nextTask);
                    //System.out.println("Picked up :  " + nextTask.toString() + " (picked up at " + currentTimeStep + ")");
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
                            //System.out.println("Move3 --> " + city.toString());
                        }
                        currentCity = taskToDeliver.deliveryCity;
                        plan.appendDelivery(taskToDeliver);
                        //System.out.println("Delivered :  " + taskToDeliver.toString() + " (delivered at " + currentTimeStep + ")");
                    }
                    currentTimeStep++;
                    currentTask = taskToDeliver;
                } while(taskToDeliver != null);
            }
            plans.add(plan);
        }
        return plans; 
    }

    
    // Check that this instance satisfies the constraints
    public boolean isValid(TaskSet tasks, List<Vehicle> vehicles) {   
    	
        // All tasks must be delivered
        //System.out.println("Bonjour je suis isValid");
        if (!attributeSizeValid(tasks, vehicles)) { return false; }
        //System.out.println("ATTRIBUTE SIZE IS VALID");
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
        //System.out.println("nextTaskForTask IS VALID");
        for (Map.Entry<Integer, Task> set : nextTaskForVehicle.entrySet()) {
            if (set.getValue() != null) {
                // The first task of a vehicle must be picked up at time step 0
                if (pickupTimes.get(set.getValue()) != 0) { return false;}
                // The first task of a vehicle must be delivered by this vehicle
                if (this.vehicles.get(set.getValue()) != set.getKey()) { return false;}
            }
        }

        //System.out.println("nextTaskForVehicle IS VALID");
        for (Map.Entry<Task, Integer> set : pickupTimes.entrySet()) {
            // The delivery time step of a task must be higher than its pickup time step
            if (deliveryTimes.get(set.getKey()) <= set.getValue()) { return false; }
        }
        //System.out.println("pickupTimes IS VALID");
        
        int timeStep = 1;
        Double[] pickedUpTasksWeight = new Double[vehicles.size()]; 
        Arrays.fill(pickedUpTasksWeight, 0.0);
        while(updateTasksAtTimeStepForVehicles(timeStep, vehicles)) {
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
                }
            }
            timeStep++;
        }
        //System.out.println("pickedUpTasksWeight IS VALID");
        return true; 
    }

    // Returns all the tasks performed by a given vehicle at a given timetep 
    private boolean updateTasksAtTimeStepForVehicles(Integer timeStep, List<Vehicle> vehicles) {
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
    private boolean attributeSizeValid(TaskSet tasks, List<Vehicle> vehicles) {
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
}