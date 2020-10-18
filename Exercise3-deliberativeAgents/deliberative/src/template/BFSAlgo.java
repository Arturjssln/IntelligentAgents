package template;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.State;

public class BFSAlgo {

    // List type that allows removing at the beggining 
    LinkedList<State> statesToCheck; 
    ArrayList<State> C; //TODO: what is C ? 


    private Plan computePlan(Vehicle vehicle, TaskSet tasks) {

        State currentState = new State(vehicle.getCurrentCity()); 
        currentState.setPickedUpTasks(vehicle.getCurrentTasks());
        currentState.setAwaitingDeliveryTasks(tasks);

        statesToCheck = new LinkedList<State>(); 
        statesToCheck.add(currentState);
        C = new ArrayList<State>();

		do {
            if (statesToCheck.isEmpty()) break; 
            
            State stateToCheck = statesToCheck.removeFirst(); // TODO: on peut changer le nom si ça te dérange

            // We check if node is the last task to perform 
            if (stateToCheck.isLastTask()) {
				return stateToCheck.getPlan(); 
            }
            if (!C.contains(stateToCheck)) { //TODO: what is C ?
                C.add(stateToCheck);
                successorStates = computeSuccessors(stateToCheck);
                statesToCheck.add(successorStates)
            }
        } while (true);
		throw new IndexOutOfBoundsException('Q-Table is empty') 
		
    }

    private LinkedList<State> computeSuccessors(State state) {
        // Compute a list of all possible states given the current state
        TaskSet awaitingDeliveryTasks = state.getAwaitingDeliveryTasks();
        TaskSet pickedUpTasks = state.getPickedUpTasks();
        
        LinkedList<State> nextStates = new LinkedList<State>();

        // Browse all available task 
        for (Task task : awaitingDeliveryTasks) {
            // TODO
            State nextState = new State(); // See if there is a way to copy task different than our inspiration
            // 1. Check tasks available in current city (i.e. state.getCurrentCity())
            // 2. For all cities on the path to task.pickupCity 
            // 2.1. Check if there are tasks to deliver on the way
            // 2.2 Check if there are tasks to pickup on the way

            nextStates.add(nextState);
        }

        // Deliver picked up tasks
        for (Task task : pickedUpTasks) {
            // TODO 
            State nextState = new State(); // See if there is a way to copy task different than our inspiration

            // 1. Go to deliver city, one step at a time
            // 2. For all cities on the path to task.deliveryCity 
            // 2.1. Check if there are tasks to deliver on the way
            // 2.2 Check if there are tasks to pickup on the way 

            nextStates.add(nextState);
        }

    }

    /*
    private List<City> computeSuccessors(State state, Vehicle vehicle, TaskSet tasks) {
        // Create a list of the possible actions for the given state
        // Successors can be 'move to another city', 'pickup task'
		City currentCity = state.getCurrentCity(); 
		List<TaskStatut> currentTasks = state.getCurrentTasks();
        TaskSet currentTasks = tasks; 
		List<City> citiesTo = new List<City>();
 
			// For all tasks 
			for (Task task : tasks) {
                
                // Can go to the cities where task to deliver
				for (City cityTo : current.pathTo(task.pickupCity)) {
					// Save the cities where agent can go to get a task 
					citiesTo.add(cityTo);
				}				
			}
		}
    }
    */
}