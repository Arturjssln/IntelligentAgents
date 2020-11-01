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

    // Sort List of Vehicles by increasing order of capacity
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
    
    // Sort List of costs by increasing order and return index if sorted list
	private int[] sortByCost(List<Double> costsForSolution) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, costsForSolution.size())
			.boxed().sorted((i, j) -> Double.valueOf(costsForSolution.get(j)).compareTo(Double.valueOf(costsForSolution.get(i))))
            .mapToInt(ele -> ele).toArray();
		return sortedIndices; 
	}

    private List<Solution> generateNeighbours(Solution solution) {
        List<Solution> neighours = new ArrayList<Solution>();
        Vehicle vehicleI; 
        do {
            Random rand = new Random(); 
            vehicleI = vehicles.get(rand.nextInt(vehicles.size()));
        } while (solution.nextTaskForVehicle.get(vehicleI) == null); // TODO: check here if null is returned

        Task task = solution.nextTaskForVehicle.get(vehicleI);
        for (Vehicle vehicleJ : vehicles) {
            if (!vehicleJ.equals(vehicleI)) {
                if (task.weight < vehicleJ.capacity()) { //TODO!: check this condition
                    neighours.add(changingVehicle(solution, vehicleI, vehicleJ));
                }
            }
        }
        int length = 0;
        do {
            task = solution.nextTaskForTask.get(task);
            length++;
        } while(task == null);

        if (length > 2) {
            for (int tIdx1=0; tIdx1<length-1; tIdx1++) {
                for (int tIdx2=tIdx1+1; tIdx2<length; tIdx2++) {
                    neighours.add(changingTaskOrder(solution, vehicleI, tIdx1, tIdx2));
                }
            }
        }
    	return neighours;
    }

    private Solution changingVehicle(Solution solution, Vehicle v1, Vehicle v2) {
        Solution newSolution = new Solution(solution);
        Task firstTask = newSolution.nextTaskForVehicle.get(v1); 

        newSolution.nextTaskForVehicle.put(v1.id(), newSolution.nextTaskForTask.get(firstTask));
        newSolution.nextTaskForVehicle.put(v2.id(), firstTask);
        newSolution.nextTaskForTask.put(firstTask, newSolution.nextTaskForVehicle.get(v2));

        updateTime(newSolution, v1);

        newSolution.vehicles.put(firstTask, v2.id());

        return newSolution;
    }

    private Solution changingTaskOrder(Solution solution, Vehicle vehicle, int tIdx1, int tIdx2) {
        Solution newSolution = new Solution(solution);
        Task prevTask1 = newSolution.nextTaskForVehicle.get(vehicle);
        Task task1 = newSolution.nextTaskForTask.get(prevTask1);
        int count = 1;
        while (count < tIdx1) {
            prevTask1 = task1;
            task1 = newSolution.nextTaskForTask.get(prevTask1);
            count++;
        }
        Task postTask1 = newSolution.nextTaskForTask.get(task1);
        Task prevTask2 = task1;
        Task task2 = newSolution.nextTaskForTask.get(prevTask2);
        count++;
        while (count < tIdx2) {
            prevTask2 = task2;
            task2 = newSolution.nextTaskForTask.get(prevTask2);
            count++;
        }
        Task postTask2 = newSolution.nextTaskForTask.get(task2);
        if (postTask1.equals(task2)) {
            newSolution.nextTaskForTask.put(prevTask1, task2);
            newSolution.nextTaskForTask.put(task2, task1);
            newSolution.nextTaskForTask.put(task1, postTask2);
        } else {
            newSolution.nextTaskForTask.put(prevTask1, task2);
            newSolution.nextTaskForTask.put(task2, postTask1);
            newSolution.nextTaskForTask.put(prevTask2, task1);
            newSolution.nextTaskForTask.put(task1, postTask2);
        }
        updateTime(newSolution, vehicle);
        return newSolution;
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
    
    private void updateTime(Solution solution, Vehicle vehicle) {
        //TODO! : Adapt this code for multiple task
        Task taskI = solution.nextTaskForVehicle.get(vehicle);
        Task taskJ = null;
        if (taskI != null) {
            solution.pickupTimes.put(taskI, 0);
            do {
                taskJ = solution.nextTaskForTask.get(taskI);
                if (taskJ != null) {
                    solution.deliveryTimes.put(taskJ, solution.deliveryTimes.get(taskI)+1);
                    taskI = taskJ;
                } 
            } while (taskJ != null);
        }
    }
}
