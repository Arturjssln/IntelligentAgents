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

    // one variable for every existing task, and one variable for every existing vehicle
    public HashMap<Task, Task> nextTaskForTask;
    // one variable for every existing task, and one variable for every existing vehicle
    public HashMap<Integer, Task> nextTaskForVehicle; // TODO: vérifier qu'il y a null a l'initialization
    // one variable for each task : order of delivery
    public HashMap<Task, Integer> pickupTimes;
    public HashMap<Task, Integer> deliveryTimes;
    // one variable for each task : which vehicle for which task ?
    public HashMap<Task, Integer> vehicles;

    private List<List<Task>> tasksAtTimeStepForVehicles;

    public Solution() {
        this.nextTaskForTask = new HashMap<Task, Task>();
        this.nextTaskForVehicle = new HashMap<Integer, Task>();
        this.pickupTimes = new HashMap<Task, Integer>();
        this.deliveryTimes = new HashMap<Task, Integer>();
        this.vehicles = new HashMap<Task, Integer>();
    }

    // TODO : verify that copy is enough !!
    @SuppressWarnings("unchecked")
    // Copy constructor
	public Solution(Solution solution) {
        this.nextTaskForTask = (HashMap<Task, Task>)solution.getNextTaskForTask().clone();
        this.nextTaskForVehicle = (HashMap<Integer, Task>)solution.getNextTaskForVehicle().clone();
        this.pickupTimes = (HashMap<Task, Integer>)solution.getPickupTimes().clone();
        this.deliveryTimes = (HashMap<Task, Integer>)solution.getDeliveryTimes().clone();
        this.vehicles = (HashMap<Task, Integer>)solution.getVehicles().clone();
    }


    public List<Plan> generatePlans(List<Vehicle> vehicles) {
        // generate plan based on the attributes of the solution 
        
        List<Plan> plans = new ArrayList<Plan>();
        
        for (Vehicle vehicle : vehicles) {
            // Create a plan for each vehicle
            Plan plan = new Plan(vehicle.getCurrentCity());
            // Set first task
            Task currentTask = nextTaskForVehicle.get(vehicle.id());

            // Go to pickup city and pick up the first task  
            for (City city : vehicle.getCurrentCity().pathTo(currentTask.pickupCity)) {
                plan.appendMove(city);
            }
            plan.appendPickup(currentTask);

            // Browse tasks to do while there are still some
            Task taskToDeliver = null;
            int currentTimeStep = 1;
            while(nextTaskForTask.get(currentTask) != null) {
                
                // Save next task based on current one
                Task nextTask = nextTaskForTask.get(currentTask);
                int pickupTimeNextTask = pickupTimes.get(nextTask);
                // While next task isn't to be picked up
                while(pickupTimeNextTask > currentTimeStep) {
                    // We know there is a task to deliver, else would have to pick up next one 
                    // Among tasks to deliver, find task that need to be delivered at current time
                    for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                        if (set.getValue().equals(currentTimeStep)) {
                            // If task to deliver at current time 
                            taskToDeliver = set.getKey();
                        }
                    }
                    // Go to deliver city and deliver task 
                    for (City city : currentTask.deliveryCity.pathTo(taskToDeliver.deliveryCity)) {
                        plan.appendMove(city);
                    }
                    plan.appendDelivery(taskToDeliver);
                    currentTask = taskToDeliver;
                    currentTimeStep++;
                }

                // Time to move and pickup next task
                for (City city : currentTask.deliveryCity.pathTo(nextTask.pickupCity)) {
                    plan.appendMove(city);
                }
                plan.appendPickup(nextTask);
                currentTask = nextTask;
                currentTimeStep++;
            }
            //TODO: improve this loop
            do {
                // If still tasks to deliver
                taskToDeliver = null;
                for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                    if (set.getValue().equals(currentTimeStep)) {
                        taskToDeliver = set.getKey();
                    }
                }

                if (taskToDeliver != null) {
                    // Deliver all tasks that are not delivered
                    for (City city : currentTask.deliveryCity.pathTo(taskToDeliver.deliveryCity)) {
                        plan.appendMove(city);
                    }
                    plan.appendDelivery(taskToDeliver);
                }
                currentTimeStep++;
                currentTask = taskToDeliver;
            } while(taskToDeliver != null);
 
            plans.add(plan);
        }
        return plans; 
    }

    
    public boolean isValid(TaskSet tasks, List<Vehicle> vehicles) {
        // Respects the constraints 
    
    	
        // if the attributes are of the good size 
        // all tasks must be delivered
        if (!attributeSizeValid(tasks, vehicles)) { return false; }
            
        for (Map.Entry<Task, Task> set : nextTaskForTask.entrySet()) {
            // nextTask(t) != t: task delivered after some task t cannot be the same task
            if (set.getKey() == set.getValue()) { return false; }
            // nextTask(ti) = tj && tj != null ⇒ timePickup(tj) > timePickup(ti)
            if (set.getValue() != null) {
                if (pickupTimes.get(set.getValue()) <= pickupTimes.get(set.getKey())) { return false;}
                if (deliveryTimes.get(set.getValue()) <= deliveryTimes.get(set.getKey())) { return false;}
            }
            // nextTask(ti) = tj ⇒ vehicle(tj) = vehicle(ti)
            if ((set.getValue() != null) && (this.vehicles.get(set.getValue()) != this.vehicles.get(set.getKey()))) { return false;}
        }

        for (Map.Entry<Integer, Task> set : nextTaskForVehicle.entrySet()) {
            if (set.getValue() != null) {
                // nextTask(vk) = tj ⇒ time(tj ) = 1
                if (pickupTimes.get(set.getValue()) != 1) { return false;}
                // nextTask(vk) = tj ⇒ vehicle(tj) = vk
                if (this.vehicles.get(set.getValue()) != set.getKey()) { return false;}
            }
        }

        for (Map.Entry<Task, Integer> set : pickupTimes.entrySet()) {
            // check that pickup is before delivery for the same task 
            // timePickup(ti) < timeDelivery(ti)
            if (deliveryTimes.get(set.getKey()) <= set.getValue()) { return false; }
        }

        // check that multiple actions done by a same vehicle 
        // capacity of a vehicle cannot be exceeded
        int timeStep = 1;
        Double[] pickedUpTasksWeight = new Double[vehicles.size()]; 
        Arrays.fill(pickedUpTasksWeight, 0.0);
        while(updateTasksAtTimeStepForVehicles(timeStep, vehicles)) {
            for (int i=0; i<vehicles.size(); i++) {
                List<Task> tasksAtTimeStep = tasksAtTimeStepForVehicles.get(i);
                if (tasksAtTimeStep.size() > 1) { return false;}
                if (tasksAtTimeStep.size() == 1) {
                    Task task = tasksAtTimeStep.get(0);
                    if (pickupTimes.get(task) == timeStep) {
                        pickedUpTasksWeight[i] += task.weight;
                    } else {
                        pickedUpTasksWeight[i] -= task.weight;
                    }
                    if (pickedUpTasksWeight[i] > vehicles.get(i).capacity()) { return false;}
                }
            }
            timeStep++;
        }
        return true; 
    }

    private boolean updateTasksAtTimeStepForVehicles(Integer timeStep, List<Vehicle> vehicles) {
        List<Task> tasksAtTime = new ArrayList<Task>(); 
        tasksAtTime.addAll(getTasksAtTime(pickupTimes, timeStep));
        tasksAtTime.addAll(getTasksAtTime(deliveryTimes, timeStep));

        List<List<Task>> tasksAtTimeStepForVehicles = new ArrayList<List<Task>>(vehicles.size());
        for (Task task : tasksAtTime) {
        	tasksAtTimeStepForVehicles.get(this.vehicles.get(task)).add(task);
        }
        this.tasksAtTimeStepForVehicles = tasksAtTimeStepForVehicles;
        return (!tasksAtTime.isEmpty()); 
    }

    private boolean attributeSizeValid(TaskSet tasks, List<Vehicle> vehicles) {
        return ((vehicles.size() == nextTaskForVehicle.size()) && 
                (tasks.size() == nextTaskForTask.size()) && 
                (tasks.size() == pickupTimes.size()) &&
                (tasks.size() == deliveryTimes.size()) && 
                (tasks.size() == this.vehicles.size()));
    }

    // GETTERS 
    public HashMap<Task, Task> getNextTaskForTask() {
        return nextTaskForTask; 
    }

    public HashMap<Integer, Task> getNextTaskForVehicle() {
        return nextTaskForVehicle; 
    }

    public HashMap<Task, Integer> getPickupTimes() {
        return pickupTimes; 
    }

    public HashMap<Task, Integer> getDeliveryTimes() {
        return deliveryTimes; 
    }

    public HashMap<Task, Integer> getVehicles() {
        return vehicles; 
    }     

    // SETTERS 
    public void setNextTaskForTask(HashMap<Task, Task> nextTaskForTask) {
        this.nextTaskForTask = nextTaskForTask;
    }

    public void setNextTaskForVehicle(HashMap<Integer, Task> nextTaskForVehicle) {
        this.nextTaskForVehicle = nextTaskForVehicle;
    }

    public void setPickupTimes(HashMap<Task, Integer> pickupTimes) {
        this.pickupTimes = pickupTimes;
    }

    public void setDeliveryTimes(HashMap<Task, Integer> deliveryTimes) {
        this.deliveryTimes = deliveryTimes;
    }

    public void setVehicles(HashMap<Task, Integer> vehicles) {
        this.vehicles = vehicles;
    }


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