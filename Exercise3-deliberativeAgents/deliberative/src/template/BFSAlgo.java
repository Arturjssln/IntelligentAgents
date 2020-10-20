package template;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.Algo;
import template.State;
import template.DeliberativeAgent.Heuristic;

public class BFSAlgo extends Algo {
	
	int costPerKm;
	int vehicleCapacity;

	// Constructor
	public BFSAlgo(Vehicle vehicle) {
		this.vehicleCapacity = vehicle.capacity(); 
		this.costPerKm = vehicle.costPerKm(); 
	}
	
    @Override
    public Plan computePlan(Vehicle vehicle, TaskSet tasks) {
        // Add the tasks that were not picked up yet to the awaiting delivery list
        TaskSet awaitingDeliveryTasks = tasks;
        for (Task task : awaitingDeliveryTasks) {
            if (vehicle.getCurrentTasks().contains(task)) {
                awaitingDeliveryTasks.remove(task);
            }
        }
        State currentState = new State(vehicle.getCurrentCity(), vehicle.getCurrentTasks(), awaitingDeliveryTasks); 
        
        LinkedList<State> statesToCheck = new LinkedList<State>();
        statesToCheck.add(currentState); 
        ArrayList<State> statesChecked = new ArrayList<State>(); 

		do {
			if (statesChecked.size()%10000 == 0) {
				System.out.println("States checked: " + statesChecked.size() + " States to check: " + statesToCheck.size());
			}
            if (statesToCheck.isEmpty()) break; 
            
            State stateToCheck = statesToCheck.removeFirst(); 
            // We check if node is the last task to perform 
            if (stateToCheck.isLastTask()) {
            	System.out.println("Total states checked: " + statesChecked.size());
				return stateToCheck.getPlan(); 
            }
            if (!statesChecked.contains(stateToCheck)) { 
                statesChecked.add(stateToCheck);
                LinkedList<State> successorStates = computeSuccessors(stateToCheck);
                statesToCheck.addAll(successorStates);
            }
        } while (true);
		throw new IndexOutOfBoundsException("BFS StatesToCheck is empty");
    }

    @Override
    protected LinkedList<State> computeSuccessors(State state) {
        // Compute a list of all possible states given the current state
        TaskSet awaitingDeliveryTasks = state.getAwaitingDeliveryTasks();
        TaskSet pickedUpTasks = state.getPickedUpTasks();
        
        LinkedList<State> nextStates = new LinkedList<State>();

        // Browse all available awaiting tasks
        for (Task task : awaitingDeliveryTasks) {
            State nextState = new State(state);

            // If there are awaiting tasks in the currentCity 
            for (Task taskToPickup : getTasksToPickup(state)) {
                // If vehicle has enough capacity to pick up the task 
                if (nextState.getPickedUpTasks().weightSum() + task.weight + taskToPickup.weight < vehicleCapacity && task != taskToPickup) {
                    // Pick up the task by Setting nextState extra plan step, pickup task and remove the picked up task from awaiting tasks
                    nextState.plan.appendPickup(taskToPickup);
                    nextState.pickedUpTasks.add(taskToPickup);
                    nextState.awaitingDeliveryTasks.remove(taskToPickup);
                }
            }
            // If tasks to deliver or pickup on the way to task.deliveryCity
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.pickupCity)) {
                // for each city on the way, move to from city to city is added
                nextState.setCurrentCity(cityOnTheWay);
                nextState.plan.appendMove(cityOnTheWay);
                // For all the picked up tasks to be delivered in the cityOnTheWay
                for (Task taskToDeliver : getTasksToDeliver(nextState)) {
                    // Deliver the task and remove it from the picked up tasks
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }
                // For all the awaiting tasks in the cityOnTheWay
                for (Task taskToPickup : getTasksToPickup(nextState)) {
                    // If the vehicle has enough capacity to pick up the task
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight + task.weight < vehicleCapacity && task != taskToPickup) {
                        // Pick up the task by setting nextState extra plan step, pickup task and remove the picked up task from awaiting tasks
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.pickedUpTasks.add(taskToPickup); 
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                    }
                }
            }
            
            // If the vehicle has enough capacity to pick up the task
            if (nextState.getPickedUpTasks().weightSum() + task.weight < vehicleCapacity) {
                // Pick up the initial task 'task' by setting the plan, removing the task from awaiting and adding it to picked up
                nextState.plan.appendPickup(task);
                nextState.awaitingDeliveryTasks.remove(task); 
                nextState.pickedUpTasks.add(task); 
                // Save the corresponding state 
                nextStates.add(nextState);
            }
        }


        // Browse all picked up tasks
        for (Task task : pickedUpTasks) {
            State nextState = new State(state);
            // Check all cities on the path 
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.deliveryCity)) {
                // Add going to the city to the plan 
                nextState.setCurrentCity(cityOnTheWay);
                nextState.plan.appendMove(cityOnTheWay);
                // For all tasks to be pickup
                for (Task taskToPickup : getTasksToPickup(nextState)) {
                    // If vehicle has enough capacity to pick up the task 
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight < vehicleCapacity) {
                        // Pick up by adding the action to the plan, and update the TaskSets
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
                // For all taks to deliver
                for (Task taskToDeliver : getTasksToDeliver(nextState)) {
                    // Deliver the task by adding the action to the plan and removing it from the picked up task
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }
                  
            } 
            // Save the corresponding state 
            nextStates.add(nextState);
        }
        return nextStates;
    }
}