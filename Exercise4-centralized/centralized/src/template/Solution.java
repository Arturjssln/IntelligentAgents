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
    public HashMap<Integer, Task> nextTaskForVehicle; // TODO: vérifier qu'il y a null a l'initialization
    // one variable for every existing task, and one variable for every existing vehicle
    public HashMap<Task, Task> nextTaskForTask;
      // one variable for each task : order of delivery
    public HashMap<Task, Integer> pickupTimes;
    public HashMap<Task, Integer> deliveryTimes;
    // one variable for each task : which vehicle for which task ?
    public HashMap<Task, Integer> vehicles;

    private ArrayList<ArrayList<Task>> tasksAtTimeStepForVehicles;

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
        this.nextTaskForTask = new HashMap<Task, Task>(solution.nextTaskForTask);
        this.nextTaskForVehicle = new HashMap<Integer, Task>(solution.nextTaskForVehicle);
        this.pickupTimes = new HashMap<Task, Integer>(solution.pickupTimes);
        this.deliveryTimes = new HashMap<Task, Integer>(solution.deliveryTimes);
        this.vehicles = new HashMap<Task, Integer>(solution.vehicles);
        // this.nextTaskForTask = (HashMap<Task, Task>)solution.getNextTaskForTask().clone();
        // this.nextTaskForVehicle = (HashMap<Integer, Task>)solution.getNextTaskForVehicle().clone();
        // this.pickupTimes = (HashMap<Task, Integer>)solution.getPickupTimes().clone();
        // this.deliveryTimes = (HashMap<Task, Integer>)solution.getDeliveryTimes().clone();
        // this.vehicles = (HashMap<Task, Integer>)solution.getVehicles().clone();
    }

    public List<Plan> generatePlans(List<Vehicle> vehicles) {
        // generate plan based on the attributes of the solution 
        
        List<Plan> plans = new ArrayList<Plan>();
        //System.out.println("Sizes: " + nextTaskForTask.size() + " " + nextTaskForVehicle.size() + " " +  pickupTimes.size() + " " + deliveryTimes.size() + " " + vehicles.size());
        for (Vehicle vehicle : vehicles) {
            // Create a plan for each vehicle
            Plan plan = new Plan(vehicle.getCurrentCity());
            // Set first task
            Task currentTask = nextTaskForVehicle.get(vehicle.id());
            if (currentTask != null) {
                //System.out.println("First task : " + currentTask.toString());
                // Go to pickup city and pick up the first task  
                for (City city : vehicle.getCurrentCity().pathTo(currentTask.pickupCity)) {
                    plan.appendMove(city);
                    //System.out.println("Move0 --> " + city.toString());
                }
                plan.appendPickup(currentTask);
                // Browse tasks to do while there are still some
                Task taskToDeliver = null;
                int currentTimeStep = 1;
                //System.out.println("Picked up :  " + currentTask.toString() + " (picked up at 0)");

                while(nextTaskForTask.get(currentTask) != null) {
                    // Save next task based on current one
                    Task nextTask = nextTaskForTask.get(currentTask);
                    int pickupTimeNextTask = pickupTimes.get(nextTask);
                    //System.out.println("Current timestep : " + currentTimeStep);
                    //System.out.println("Next task :  " + nextTask.toString() + " will be picked up at " + pickupTimeNextTask);
                    // While next task isn't to be picked up
                    while(pickupTimeNextTask > currentTimeStep) {
                        // We know there is a task to deliver, else would have to pick up next one 
                        // Among tasks to deliver, find task that need to be delivered at current time
                        for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                            if (set.getValue().equals(currentTimeStep) && (this.vehicles.get(set.getKey()) == vehicle.id())) {
                                // If task to deliver at current time 
                                taskToDeliver = set.getKey();
                            }
                        }
                        // Go to deliver city and deliver task 
                        //System.out.println(currentTask.deliveryCity.toString() + " --> " + taskToDeliver.deliveryCity.toString());
                        for (City city : currentTask.pickupCity.pathTo(taskToDeliver.deliveryCity)) {
                            plan.appendMove(city);
                            //System.out.println("Move1 --> " + city.toString());
                        }
                        plan.appendDelivery(taskToDeliver);
                        //System.out.println("Delivered :  " + taskToDeliver.toString() + " ( delivered at " + currentTimeStep + ")");
                        currentTask = taskToDeliver;
                        currentTimeStep++;
                    }

                    // Time to move and pickup next task
                    for (City city : currentTask.deliveryCity.pathTo(nextTask.pickupCity)) {
                        plan.appendMove(city);
                        //System.out.println("Move2 --> " + city.toString());
                    }
                    plan.appendPickup(nextTask);
                    //System.out.println("Picked up :  " + nextTask.toString() + " (picked up at " + currentTimeStep + ")");
                    currentTask = nextTask;
                    currentTimeStep++;
                }
                do {
                    // If still tasks to deliver
                    taskToDeliver = null;
                    for (Map.Entry<Task, Integer> set : deliveryTimes.entrySet()) {
                        if (set.getValue().equals(currentTimeStep) && (this.vehicles.get(set.getKey()) == vehicle.id())) {
                            taskToDeliver = set.getKey();
                        }
                    }

                    if (taskToDeliver != null) {
                        // Deliver all tasks that are not delivered
                        for (City city : currentTask.deliveryCity.pathTo(taskToDeliver.deliveryCity)) {
                            plan.appendMove(city);
                            //System.out.println("Move3 --> " + city.toString());
                        }
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

    
    public boolean isValid(TaskSet tasks, List<Vehicle> vehicles) {
        // Respects the constraints 
    
    	
        // if the attributes are of the good size 
        // all tasks must be delivered
        System.out.println("Bonjour je suis isValid");
        if (!attributeSizeValid(tasks, vehicles)) { return false; }
        System.out.println("ATTRIBUTE SIZE IS VALID");
        for (Map.Entry<Task, Task> set : nextTaskForTask.entrySet()) {
            // nextTask(t) != t: task delivered after some task t cannot be the same task
            if (set.getKey() == set.getValue()) { return false; }
            // nextTask(ti) = tj && tj != null ⇒ timePickup(tj) > timePickup(ti)
            if (set.getValue() != null) {
                if (pickupTimes.get(set.getValue()) <= pickupTimes.get(set.getKey())) { return false;}
                // if (deliveryTimes.get(set.getValue()) <= deliveryTimes.get(set.getKey())) { return false;}
            }
            // nextTask(ti) = tj ⇒ vehicle(tj) = vehicle(ti)
            if ((set.getValue() != null) && (this.vehicles.get(set.getValue()) != this.vehicles.get(set.getKey()))) { return false;}
        }
        //System.out.println("nextTaskForTask IS VALID");
        for (Map.Entry<Integer, Task> set : nextTaskForVehicle.entrySet()) {
            if (set.getValue() != null) {
                // nextTask(vk) = tj ⇒ time(tj ) = 0
                if (pickupTimes.get(set.getValue()) != 0) { return false;}
                // nextTask(vk) = tj ⇒ vehicle(tj) = vk
                if (this.vehicles.get(set.getValue()) != set.getKey()) { return false;}
            }
        }

        //System.out.println("nextTaskForVehicle IS VALID");
        for (Map.Entry<Task, Integer> set : pickupTimes.entrySet()) {
            // check that pickup is before delivery for the same task 
            // timePickup(ti) < timeDelivery(ti)
            if (deliveryTimes.get(set.getKey()) <= set.getValue()) { return false; }
        }
        //System.out.println("pickupTimes IS VALID");
        
        int timeStep = 1;
        Double[] pickedUpTasksWeight = new Double[vehicles.size()]; 
        Arrays.fill(pickedUpTasksWeight, 0.0);
        while(updateTasksAtTimeStepForVehicles(timeStep, vehicles)) {
            for (int i=0; i<vehicles.size(); i++) {
                List<Task> tasksAtTimeStep = tasksAtTimeStepForVehicles.get(i);
                // check that multiple actions done by a same vehicle 
                if (tasksAtTimeStep.size() > 1) { return false;}
                // capacity of a vehicle cannot be exceeded
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
        System.out.println("pickedUpTasksWeight IS VALID");
        return true; 
    }

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

    private boolean attributeSizeValid(TaskSet tasks, List<Vehicle> vehicles) {
        return (/*(vehicles.size() == nextTaskForVehicle.size()) && */
                (tasks.size() == nextTaskForTask.size()) && 
                (tasks.size() == pickupTimes.size()) &&
                (tasks.size() == deliveryTimes.size()) && 
                (tasks.size() == this.vehicles.size()));
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