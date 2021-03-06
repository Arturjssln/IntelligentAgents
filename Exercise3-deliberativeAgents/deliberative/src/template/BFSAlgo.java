package template;

import java.util.LinkedList;
import java.util.ArrayList;

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
            if (stateToCheck.isGoalState()) {
                System.out.println("Total states checked: " + statesChecked.size());
                System.out.println("Total cost of plan: " + stateToCheck.getCost());
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

            // If there are awaiting tasks in the currentCity , pickup them
            for (Task taskToPickup : getTasksToPickup(state)) {
                // Check vehicle capacity
                if (nextState.getPickedUpTasks().weightSum() + task.weight + taskToPickup.weight < vehicleCapacity && task != taskToPickup) {
                    nextState.plan.appendPickup(taskToPickup);
                    nextState.pickedUpTasks.add(taskToPickup);
                    nextState.awaitingDeliveryTasks.remove(taskToPickup);
                }
            }

            // If tasks to deliver or pickup on the way to task.pickupCity
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.pickupCity)) {
                nextState.setCurrentCity(cityOnTheWay);
                nextState.plan.appendMove(cityOnTheWay);

                // Check if there are task to deliver in the current city
                for (Task taskToDeliver : getTasksToDeliver(nextState)) {
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }

                // Check if there are task to pickup in the current city
                for (Task taskToPickup : getTasksToPickup(nextState)) {
                    // Check vehicle capacity
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight + task.weight < vehicleCapacity && task != taskToPickup) {
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.pickedUpTasks.add(taskToPickup); 
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                    }
                }
            }

            // If the vehicle has enough capacity to pick up the task
            if (nextState.getPickedUpTasks().weightSum() + task.weight < vehicleCapacity) {
                nextState.plan.appendPickup(task);
                nextState.awaitingDeliveryTasks.remove(task); 
                nextState.pickedUpTasks.add(task); 
                nextState.computeCost(costPerKm); // update cost in state
                
                // Save the corresponding state 
                nextStates.add(nextState);
            }
        }


        // Browse all picked up tasks
        for (Task task : pickedUpTasks) {
            State nextState = new State(state);
            
            // For all city on the way to the delivery city
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.deliveryCity)) {
                nextState.setCurrentCity(cityOnTheWay);
                nextState.plan.appendMove(cityOnTheWay);

                // Check if there are task to pickup in the current city
                for (Task taskToPickup : getTasksToPickup(nextState)) {
                    // Check vehicle capacity
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight < vehicleCapacity) {
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
                // For all taks to deliver
                for (Task taskToDeliver : getTasksToDeliver(nextState)) {
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }

            } 
            nextState.computeCost(costPerKm);
            // Save the corresponding state 
            nextStates.add(nextState);
        }
        return nextStates;
    }
}