package template;

import java.util.ArrayList;
import java.util.LinkedList;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import template.Algo;

public abstract class Algo {

    // Abstract methods overrided in the subclasses
    public abstract Plan computePlan(Vehicle vehicle, TaskSet tasks);
    protected abstract LinkedList<State> computeSuccessors(State state);
    
    // Utility methods 
    protected ArrayList<Task> getTasksToPickup(State state) {
        // Return the tasks awaiting to be picked up in state
    	ArrayList<Task> tasksFromCity = new ArrayList<Task>();
        for(Task task : state.getAwaitingDeliveryTasks()) {
            if (state.getCurrentCity() == task.pickupCity) {
                tasksFromCity.add(task);
            }
        }
        return tasksFromCity;
    }

    protected ArrayList<Task> getTasksToDeliver(State state) {
        // Return the tasks pickedup and to be delivered in state
    	ArrayList<Task> tasksToCity = new ArrayList<Task>();
        for(Task task : state.getPickedUpTasks()) {
            if (state.getCurrentCity() == task.deliveryCity) {
                tasksToCity.add(task);
            }
        }
        return tasksToCity;
    }

}