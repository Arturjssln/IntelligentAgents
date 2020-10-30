package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.Solution;

public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    Solution currentSolution;
    List<Vehicle> vehicles;

    final int MAX_ITERATION = 10000;
    final double PROBABILITY_UPDATE_SOLUTION = 0.3; 

	// Constructor
	public SLSAlgo(List<Vehicle> vehicles, TaskSet tasks) {
        this.vehicles = vehicles;
        this.currentSolution = selectInitialSolution(tasks);
        
	}


    public List<Plan> computePlans(TaskSet tasks, long end_time) {
        List<Plan> plans = new ArrayList<Plan> ();
        long current_time = System.currentTimeMillis();

        currentSolution = selectInitialSolution(tasks);
        List<Solution> potentialSolutions = new ArrayList<Solution> ();

        int iteration = 0;
        do {
            potentialSolutions = generateNeighbours(currentSolution);
            currentSolution = localChoice(potentialSolutions);
            ++iteration;
        } while((iteration<MAX_ITERATION) && (current_time < end_time));

        plans = currentSolution.generatePlans(this.vehicles);

        // need to be of size #vehicles


        return plans;
    }


    private Solution selectInitialSolution(TaskSet tasks) {
        // return a plan that is valid
        // udpate the lists
        Solution initialSolution = new Solution();

        List<Vehicle> sortedVehicles = sortByCapacity(vehicles); 

        HashMap<Task, Integer> vehicles = new HashMap<Task, Integer>();
        HashMap<Task, Task> nextTaskForTask = new HashMap<Task, Task>();
        HashMap<Integer, Task> nextTaskForVehicle = new HashMap<Integer, Task>();
        HashMap<Task, Integer> pickupTimes = new HashMap<Task, Integer>();
        HashMap<Task, Integer> deliveryTimes = new HashMap<Task, Integer>();

        // Set vehicles
        double totalWeight = 0.0; 
        int indexVehicle = 0; 
        Task lastTask = null;
        int currentTimeStep = 0;
        for (Task task: tasks) {
            Vehicle vehicle = sortedVehicles.get(indexVehicle); 
            if (vehicle.capacity() >= task.weight + totalWeight) {
                vehicles.put(task, vehicle.id()); 
                if (lastTask != null) {
                    nextTaskForTask.put(lastTask, task);
                } else {
                    nextTaskForVehicle.put(vehicle.id(), task);
                }
                pickupTimes.put(task, currentTimeStep);
                deliveryTimes.put(task, currentTimeStep+1);

                totalWeight += task.weight; 
                lastTask = task;
                currentTimeStep += 2;
            } else {
                nextTaskForTask.put(lastTask, null);
                indexVehicle++;
                totalWeight = 0.0;
                lastTask = null;
                currentTimeStep = 0;
            }
        }
        nextTaskForTask.put(lastTask, null);
        
        initialSolution.setVehicles(vehicles); 
        initialSolution.setNextTaskForVehicle(nextTaskForVehicle); 
        initialSolution.setDeliveryTimes(deliveryTimes); 
        initialSolution.setPickupTimes(pickupTimes);         
        initialSolution.setNextTaskForTask(nextTaskForTask); 

        return new Solution();
    }

    //TODO: Verify this function
    // Sort List of states by increasing order
	private List<Vehicle> sortByCapacity(List<Vehicle> vehicles) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, vehicles.size())
			.boxed().sorted((i, j) -> Double.valueOf(vehicles.get(j).capacity()).compareTo(Double.valueOf(vehicles.get(i).capacity())))
            .mapToInt(ele -> ele).toArray();
        
        //Reorder list
        LinkedList<Vehicle> sortedVehicles = new LinkedList<Vehicle>();
		for (int index : sortedIndices) {
			sortedVehicles.add(vehicles.get(index));
		}
		return sortedVehicles; 
    }
    
    //TODO: Verify this function
    // Sort LinkedList of states by increasing order
	private int[] sortByCost(List<Double> costsForSolution) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, costsForSolution.size())
			.boxed().sorted((i, j) -> Double.valueOf(costsForSolution.get(j)).compareTo(Double.valueOf(costsForSolution.get(i))))
            .mapToInt(ele -> ele).toArray();
        
		return sortedIndices; 
	}

    private List<Solution> generateNeighbours(Solution solution) {
        // changing vehicle for the task
        // changing task order
        // others ? ...

    	return new ArrayList<Solution>();
    }

    private void changingVehicle(Solution solution, Vehicle vehicle) {
        // TODO: copy of solution !!
        Solution newSolution = new Solution(solution); //TODO: change 
        Task firstTask = solution.getNextTaskForVehicle().get(vehicle); 
    }


    private Solution localChoice(List<Solution> potentialSolutions) {
        
        List<Double> costsForSolutions = new ArrayList<Double>(); 
        for (Solution solution: potentialSolutions) {
            List<Plan> plansForSolution = solution.generatePlans(vehicles);
            costsForSolutions.add(computeCost(plansForSolution)); 
        }
        
        int[] sortedIndices = sortByCost(costsForSolutions);

        if ((new Random()).nextDouble() <= PROBABILITY_UPDATE_SOLUTION) {
            return potentialSolutions.get(sortedIndices[0]);
        }
        return currentSolution; 
    }



    private double computeCost(List<Plan> plansForSolution) {
        double costSum = 0.0; 
        for (int i=0; i<plansForSolution.size(); i++) {
            costSum += plansForSolution.get(i).totalDistance() * vehicles.get(i).costPerKm(); 
        }
        return costSum; 
    }

    
    private void updateTime(Solution solution) {

    }

}
