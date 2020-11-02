package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.IntStream;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import template.Solution;

public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    Solution currentSolution;
    List<Vehicle> vehicles;
    TaskSet tasks;

    final int MAX_ITERATION = 2;
    final double PROBABILITY_UPDATE_SOLUTION = 1.0; 

	// Constructor
	public SLSAlgo(List<Vehicle> vehicles, TaskSet tasks) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.currentSolution = selectInitialSolution();

        whenModifyingOriginalObject_thenCopyShouldNotChange();
        
	}


    public List<Plan> computePlans(TaskSet tasks, long end_time) {
        List<Plan> plans = new ArrayList<Plan> ();
        
        long current_time = System.currentTimeMillis();

        currentSolution = selectInitialSolution();
        List<Solution> potentialSolutions = new ArrayList<Solution> ();
        
        int iteration = 0;
        do {
            potentialSolutions = generateNeighbours(currentSolution);
            //System.out.println("Potential solutions computed, size : "); 
            //System.out.println(potentialSolutions.size()); 
            currentSolution = localChoice(potentialSolutions);
            //System.out.println("Local choice done");
            ++iteration;
            
        } while((iteration<MAX_ITERATION) && (current_time < end_time));

        plans = currentSolution.generatePlans(this.vehicles);

        // need to be of size #vehicles
        return plans;
    }


    private Solution selectInitialSolution() {
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
                //TODO: verify that capacity is enough
                nextTaskForTask.put(lastTask, null);
                indexVehicle++;
                totalWeight = task.weight;
                lastTask = task;
                currentTimeStep = 2;
                nextTaskForVehicle.put(sortedVehicles.get(indexVehicle).id(), task);
                vehicles.put(task, sortedVehicles.get(indexVehicle).id()); 
                pickupTimes.put(task, 0);
                deliveryTimes.put(task, 1);
            }
        }
        nextTaskForTask.put(lastTask, null);
        
        initialSolution.vehicles = vehicles; 
        initialSolution.nextTaskForVehicle = nextTaskForVehicle; 
        initialSolution.deliveryTimes = deliveryTimes; 
        initialSolution.pickupTimes = pickupTimes;         
        initialSolution.nextTaskForTask = nextTaskForTask; 

        return initialSolution;
    }


    public void whenModifyingOriginalObject_thenCopyShouldNotChange() {
        Solution solution = new Solution();
        solution = selectInitialSolution();

        Solution deepCopy = new Solution(solution);

        deepCopy.pickupTimes.put(deepCopy.nextTaskForVehicle.get(0), -1);

        System.out.println(solution.pickupTimes.equals(deepCopy.pickupTimes));
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
            int idx = rand.nextInt(vehicles.size());
            vehicleI = vehicles.get(idx);
        } while (solution.nextTaskForVehicle.get(vehicleI.id()) == null); 
        Task task = solution.nextTaskForVehicle.get(vehicleI.id());
        
        for (Vehicle vehicleJ : vehicles) {
            if (!vehicleJ.equals(vehicleI)) {
                if (task.weight < vehicleJ.capacity()) { //TODO: pas la bonne capacité: doit prendre la capacité restante
                    //System.out.println("Changing vehicleBefore"); 
                    Solution temp = changingVehicle(solution, vehicleI, vehicleJ);
                    if (temp.isValid(tasks, vehicles)) { neighours.add(temp);}
                    //System.out.println("Changing vehicleAfter"); 
                }
            }
        }
        
        //System.out.println("BeforeLength"); 
        int length = 0;
        do {
            task = solution.nextTaskForTask.get(task);
            length++;
        } while(task != null);

        if (length > 2) {
            for (int tIdx1=0; tIdx1<length-1; tIdx1++) {
                for (int tIdx2=tIdx1+1; tIdx2<length; tIdx2++) {
                	//System.out.println("Changing task order"); 
                    //TODO: check capacité: doit prendre la capacité restante
                    List<Solution> solutions = changingTaskOrder(solution, vehicleI, tIdx1, tIdx2);
                    System.out.println("Number of solutions : " + solutions.size()); 
                    neighours.addAll(solutions);
                    //neighours.addAll(changingTaskOrder(solution, vehicleI, tIdx1, tIdx2));
                }
            }
        }
    	return neighours;
    }

    private Solution changingVehicle(Solution solution, Vehicle v1, Vehicle v2) {
        System.out.println(v1.id() + "  " + v2.id());
        Solution newSolution = new Solution(solution);

        // first task of v1
        Task firstTask = newSolution.nextTaskForVehicle.get(v1.id()); 
        // and corresponding delivery time 
        Integer firstTaskDeliveryTime = newSolution.deliveryTimes.get(firstTask); 

        newSolution.nextTaskForVehicle.put(v1.id(), newSolution.nextTaskForTask.get(firstTask));
        newSolution.nextTaskForTask.put(firstTask, newSolution.nextTaskForVehicle.get(v2.id()));
        newSolution.nextTaskForVehicle.put(v2.id(), firstTask);

        // remove the old pickup and deivery 
        //newSolution.pickupTimes.remove(firstTask); 
        //newSolution.deliveryTimes.remove(firstTask); 

        // Solution where deliveryTime stays the same for both tasks 

        // TODO: faire une fonction
        for (Entry<Task, Integer> entry: newSolution.deliveryTimes.entrySet()) {
            //System.out.println(newSolution.vehicles.get(entry.getKey()));
            if (newSolution.vehicles.get(entry.getKey()) == v1.id()) {
                if (entry.getValue() < firstTaskDeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-1); 
                }
                else if (entry.getValue() >= firstTaskDeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-2); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == v2.id()) {
                if (entry.getValue() < firstTaskDeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+1); 
                }
                else if (entry.getValue() >= firstTaskDeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+2); 
                }              
            }
        }

        for (Entry<Task, Integer> entry: newSolution.pickupTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == v1.id()) {
                if (entry.getValue() < firstTaskDeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-1); 
                }
                else if (entry.getValue() >= firstTaskDeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-2); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == v2.id()) {
                if (entry.getValue() < firstTaskDeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+1); 
                }
                else if (entry.getValue() >= firstTaskDeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+2); 
                }              
            }
        }

        // add new delivery and pickup 
        newSolution.pickupTimes.put(firstTask, 0);
        newSolution.deliveryTimes.put(firstTask, firstTaskDeliveryTime);
        //newSolution.pickupTimes.put(newSolution.nextTaskForVehicle.get(v1.id()), 0); //TODO: remove

        newSolution.vehicles.put(firstTask, v2.id());

        return newSolution;
    }

    private List<Solution> changingTaskOrder(Solution solution, Vehicle vehicle, int tIdx1, int tIdx2) {
        List<Solution> newSolutions = new  ArrayList<Solution>();
        Solution newSolution = new Solution(solution);
        Task prevTask1 = newSolution.nextTaskForVehicle.get(vehicle.id());
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

        int task1PickupTime = newSolution.pickupTimes.get(task1);
        int task1DeliveryTime = newSolution.deliveryTimes.get(task1);
        int task2PickupTime = newSolution.pickupTimes.get(task2);
        int task2DeliveryTime = newSolution.deliveryTimes.get(task2);
        
        // Create new solution by switching delivery time
        if (task2PickupTime < task1DeliveryTime) {
            Solution newSolutionTemp =  new Solution(newSolution);
            newSolutionTemp.pickupTimes.put(task2, task1PickupTime);
            newSolutionTemp.pickupTimes.put(task1, task2PickupTime);
            if (newSolutionTemp.isValid(tasks, vehicles)) { newSolutions.add(newSolutionTemp);}
        }
        Solution newSolutionTemp =  new Solution(newSolution);
        newSolutionTemp.pickupTimes.put(task2, task1PickupTime);
        newSolutionTemp.deliveryTimes.put(task2, task1DeliveryTime);
        newSolutionTemp.pickupTimes.put(task1, task2PickupTime);
        newSolutionTemp.deliveryTimes.put(task1, task2DeliveryTime);
        if (newSolutionTemp.isValid(tasks, vehicles)) { newSolutions.add(newSolutionTemp);}
        
        // Create new solution by switching 
        Random random = new Random();
        int maxTime = Math.max(task1DeliveryTime, task2DeliveryTime);
        int randomTask1DeliveryTime = random.nextInt(maxTime - task1PickupTime) + task1PickupTime;
        int randomTask2DeliveryTime = random.nextInt(maxTime - task2PickupTime) + task2PickupTime;

        newSolution.pickupTimes.put(task2, task1PickupTime);
        newSolution.pickupTimes.put(task1, task2PickupTime);

        for (Entry<Task, Integer> entry: newSolution.deliveryTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask1DeliveryTime < task1DeliveryTime && 
                    entry.getValue() >= randomTask1DeliveryTime &&
                    entry.getValue() < task1DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+1); 
                } else if (randomTask1DeliveryTime > task1DeliveryTime && 
                    entry.getValue() <= randomTask1DeliveryTime &&
                    entry.getValue() > task1DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-1); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask2DeliveryTime < task2DeliveryTime && 
                    entry.getValue() >= randomTask2DeliveryTime &&
                    entry.getValue() < task2DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+1); 
                }
            }
        }

        for (Entry<Task, Integer> entry: newSolution.pickupTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask1DeliveryTime < task1DeliveryTime && 
                    entry.getValue() >= randomTask1DeliveryTime &&
                    entry.getValue() < task1DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+1); 
                } else if (randomTask1DeliveryTime > task1DeliveryTime && 
                    entry.getValue() <= randomTask1DeliveryTime &&
                    entry.getValue() > task1DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-1); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask2DeliveryTime < task2DeliveryTime && 
                    entry.getValue() >= randomTask2DeliveryTime &&
                    entry.getValue() < task2DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+1); 
                }
            }
        }
        newSolution.deliveryTimes.put(task2, randomTask1DeliveryTime);
        newSolution.deliveryTimes.put(task1, randomTask2DeliveryTime);
        if (newSolution.isValid(tasks, vehicles)) { newSolutions.add(newSolution);}
        
        return newSolutions;
    }

    private Solution localChoice(List<Solution> potentialSolutions) {
        //System.out.println("Start localChoice");
        List<Double> costsForSolutions = new ArrayList<Double>(); 
        for (Solution solution: potentialSolutions) {
            List<Plan> plansForSolution = solution.generatePlans(vehicles);
            costsForSolutions.add(computeCost(plansForSolution)); 
        }
        //System.out.println("1 localChoice");
        
        int[] sortedIndices = sortByCost(costsForSolutions);
        //System.out.println("2 localChoice");
        if ((sortedIndices.length > 0) && ((new Random()).nextDouble() <= PROBABILITY_UPDATE_SOLUTION)) {
        	System.out.println("NEW SOLUTION HAS BEEN CHOSEN !!!!!!!!");
            return potentialSolutions.get(sortedIndices[0]);
        }
        //System.out.println("3 localChoice");
        return currentSolution; 
    }

    private double computeCost(List<Plan> plansForSolution) {
        double costSum = 0.0; 
        for (int i=0; i<plansForSolution.size(); i++) {
            costSum += plansForSolution.get(i).totalDistance() * vehicles.get(i).costPerKm(); 
        }
        return costSum; 
    }
    /*
    private void updateTime(Solution solution, Vehicle vehicle) {
        //TODO! : Adapt this code for multiple task
        Task taskI = solution.nextTaskForVehicle.get(vehicle.id());
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
    */
}
