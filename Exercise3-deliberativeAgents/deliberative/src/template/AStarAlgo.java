package template;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.Algo;
import template.DeliberativeAgent.Heuristic;
import template.State;



public class AStarAlgo extends Algo {
	
	Heuristic heuristic;
	int costPerKm;
	int vehicleCapacity;

	// Constructor
	public AStarAlgo(Heuristic heuristic, Vehicle vehicle) {
		this.heuristic = heuristic; 
		this.vehicleCapacity = vehicle.capacity(); 
		this.costPerKm = vehicle.costPerKm(); 
	}

	@Override
	public Plan computePlan(Vehicle vehicle, TaskSet tasks) {

        State currentState = new State(vehicle.getCurrentCity()); 
		currentState.setPickedUpTasks(vehicle.getCurrentTasks()); 


        // Add the tasks that were not picked up yet to the 'waiting'
        for (Task task : currentState.getPickedUpTasks()) {
           if (!(tasks.contains(task))) {
               currentState.awaitingDeliveryTasks.add(task);
           }
        }
  
        LinkedList<State> statesToCheck = new LinkedList<State>();
        statesToCheck.add(currentState); 
        LinkedList<State> statesChecked = new LinkedList<State>(); 

		do {
            if (statesToCheck.isEmpty()) break; 
            
            State stateToCheck = statesToCheck.removeFirst(); 

            // We check if node is the last task to perform 
            if (stateToCheck.isLastTask()) {
				return stateToCheck.getPlan(); 
            }
			if ((!statesChecked.contains(stateToCheck)) || 
				(isCostLowerThanExistingCopy(stateToCheck, statesChecked))) { 
				
				statesChecked.addFirst(stateToCheck);
			
                LinkedList<State> successorStates = computeSuccessors(stateToCheck);
				LinkedList<State> sortedSuccessorStates = sortByCost(successorStates);
				statesToCheck.addAll(sortedSuccessorStates);
				//statesToCheck = sortByCost(statesToCheck); //TODO: check if needed
            }
        } while (true);
		throw new IndexOutOfBoundsException("StatesToCheck is empty"); 
		
	}

	private boolean isCostLowerThanExistingCopy(State stateToCheck, LinkedList<State> states) {
		for (State state : states) {
			if (state.getCurrentCity() == stateToCheck.getCurrentCity()) {
				states.remove(state);
				return (stateToCheck.getTotalCost() < state.getTotalCost()); 
			}
		}
		return false; 
	}

	private LinkedList<State> sortByCost(LinkedList<State> states) {
		int[] sortedIndices = IntStream.range(0, states.size())
			.boxed().sorted((i, j) -> Double.valueOf(states.get(i).getTotalCost()).compareTo(Double.valueOf(states.get(j).getTotalCost())))
			.mapToInt(ele -> ele).toArray();
		LinkedList<State> sortedStates = new LinkedList<State>();
		for (int index : sortedIndices) {
			sortedStates.add(states.get(index));
		}
		return sortedStates; 
	}


	private LinkedList<State> computeSuccessors(State state) {
        // Compute a list of all possible states given the current state
        TaskSet awaitingDeliveryTasks = state.getAwaitingDeliveryTasks();
        TaskSet pickedUpTasks = state.getPickedUpTasks();
        
        LinkedList<State> nextStates = new LinkedList<State>();

        // Browse all available task 
        for (Task task : awaitingDeliveryTasks) {
            State nextState = new State(state);

            
            for (Task taskToPickup : getTasksFromCity(state)) {
                if (nextState.getPickedUpTasks().weightSum() + task.weight + taskToPickup.weight < vehicleCapacity && task != taskToPickup) {
                    nextState.plan.appendPickup(taskToPickup);
                    nextState.pickedUpTasks.add(taskToPickup);
                    nextState.awaitingDeliveryTasks.remove(taskToPickup);
                }
            }
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.pickupCity)) {
                nextState.setCurrentCity(cityOnTheWay);
                nextState.plan.appendMove(cityOnTheWay);
                for (Task taskToPickup : getTasksFromCity(nextState)) {
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight + task.weight < vehicleCapacity && task != taskToPickup) {
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
                for (Task taskToDeliver : getTasksToCity(nextState)) {
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }
            }
            if (nextState.getPickedUpTasks().weightSum() + task.weight < vehicleCapacity) {
                nextState.plan.appendPickup(task);
                nextState.awaitingDeliveryTasks.remove(task); 
				nextState.pickedUpTasks.add(task);
				nextState.computeCost(costPerKm);
				nextState.computeHeuristic(heuristic); //TODO: other parameters ? 
                nextStates.add(nextState);
            }
        }


        // Deliver picked up tasks
        for (Task task : pickedUpTasks) {
            State nextState = new State(state);
            
            for (City cityOnTheWay : state.getCurrentCity().pathTo(task.deliveryCity)) { 
				nextState.setCurrentCity(cityOnTheWay);
				nextState.plan.appendMove(cityOnTheWay);
                
                for (Task taskToPickup : getTasksFromCity(nextState)) {
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight < vehicleCapacity) {
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
                for (Task taskToDeliver : getTasksToCity(nextState)) {
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }
                  
			} 
			nextState.computeCost(costPerKm);
			nextState.computeHeuristic(heuristic); //TODO: other parameters ? 
            nextStates.add(nextState);
        }
        return nextStates;
    }
	
	private ArrayList<Task> getTasksFromCity(State state) {
    	ArrayList<Task> tasksFromCity = new ArrayList<Task>();
        for(Task task : state.getAwaitingDeliveryTasks()) {
            if (state.getCurrentCity() == task.pickupCity) {
                tasksFromCity.add(task);
            }
        }
        return tasksFromCity;
    }

    private ArrayList<Task> getTasksToCity(State state) {
    	ArrayList<Task> tasksToCity = new ArrayList<Task>();
        for(Task task : state.getAwaitingDeliveryTasks()) {
            if (state.getCurrentCity() == task.deliveryCity) {
                tasksToCity.add(task);
            }
        }
        return tasksToCity;
    }




}