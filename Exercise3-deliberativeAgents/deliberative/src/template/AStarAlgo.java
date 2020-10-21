package template;

import java.util.LinkedList;
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

       // Add the tasks that were not picked up yet to the awaiting delivery list
       TaskSet awaitingDeliveryTasks = tasks;
        for (Task task : awaitingDeliveryTasks) {
            if (vehicle.getCurrentTasks().contains(task)) {
                awaitingDeliveryTasks.remove(task);
            }
        }
	   State currentState = new State(vehicle.getCurrentCity(), vehicle.getCurrentTasks(), awaitingDeliveryTasks); 
	   
        // Contains states that need to be checked
        LinkedList<State> statesToCheck = new LinkedList<State>();
        statesToCheck.add(currentState); 
        // Contains states that has been checked
        LinkedList<State> statesChecked = new LinkedList<State>(); 

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
			if ((!statesChecked.contains(stateToCheck)) || 
				(isCostLowerThanExistingCopy(stateToCheck, statesChecked))) { 
				
				statesChecked.addFirst(stateToCheck);
			
                LinkedList<State> successorStates = computeSuccessors(stateToCheck);
				LinkedList<State> sortedSuccessorStates = sortByCost(successorStates);
                // Add all successors to the states that need to be checked later
                statesToCheck.addAll(sortedSuccessorStates);
            }
        } while (true);
		throw new IndexOutOfBoundsException("AStar StatesToCheck is empty"); 
		
	}

	private boolean isCostLowerThanExistingCopy(State stateToCheck, LinkedList<State> states) {
		for (State state : states) {
            if ((state.getCurrentCity() == stateToCheck.getCurrentCity()) && 
                (stateToCheck.getTotalCost() < state.getTotalCost())) {
                states.remove(state);
                return true; 
			}
		}
		return false; 
	}

    // Sort LinkedList of states by increasing order
	private LinkedList<State> sortByCost(LinkedList<State> states) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, states.size())
			.boxed().sorted((i, j) -> Double.valueOf(states.get(j).getTotalCost()).compareTo(Double.valueOf(states.get(i).getTotalCost())))
            .mapToInt(ele -> ele).toArray();
        
        //Reorder list
        LinkedList<State> sortedStates = new LinkedList<State>();
		for (int index : sortedIndices) {
			sortedStates.add(states.get(index));
		}
		return sortedStates; 
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
            
            // For all city on the way to the pickup city
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
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
            }

            // Check vehicle capacity
            if (nextState.getPickedUpTasks().weightSum() + task.weight < vehicleCapacity) {
                nextState.plan.appendPickup(task);
                nextState.awaitingDeliveryTasks.remove(task); 
				nextState.pickedUpTasks.add(task);
				nextState.computeCost(costPerKm); // update cost in state
				nextState.computeHeuristic(heuristic, costPerKm); // update heuristic in state
                
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
                
                // Check if there are task to deliver in the current city
                for (Task taskToDeliver : getTasksToDeliver(nextState)) {
                    nextState.plan.appendDelivery(taskToDeliver);
                    nextState.pickedUpTasks.remove(taskToDeliver); 
                }

                // Check if there are task to pickup in the current city
                for (Task taskToPickup : getTasksToPickup(nextState)) {
                    // Check vehicle capacity
                    if (nextState.getPickedUpTasks().weightSum() + taskToPickup.weight < vehicleCapacity) {
                        nextState.plan.appendPickup(taskToPickup);
                        nextState.awaitingDeliveryTasks.remove(taskToPickup); 
                        nextState.pickedUpTasks.add(taskToPickup); 
                    }
                }
			} 
			nextState.computeCost(costPerKm); // update cost in state
			nextState.computeHeuristic(heuristic, costPerKm); // update heuristic in state
            
            // Save the corresponding state 
            nextStates.add(nextState);
        }
        return nextStates;
    }
}