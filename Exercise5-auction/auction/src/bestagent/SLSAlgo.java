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

import template.CentralizedAgent.Initialization;

public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    Solution currentSolution;
    List<Vehicle> vehicles;
    TaskSet tasks;

    Initialization initialization;

    final int MAX_ITERATION = 1000;
    final double PROBABILITY_UPDATE_SOLUTION = 0.4; 

	// Constructor
	public SLSAlgo(List<Vehicle> vehicles, TaskSet tasks, Initialization initialization) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.initialization = initialization;
        this.currentSolution = selectInitialSolution();

        
	}


    public List<Plan> computePlans(TaskSet tasks, long end_time) {
        List<Plan> plans = new ArrayList<Plan> ();
        
        long current_time = System.currentTimeMillis();

        currentSolution = selectInitialSolution();
        List<Solution> potentialSolutions = new ArrayList<Solution> ();
        
        int iteration = 0;
        do {
            potentialSolutions = generateNeighbours(currentSolution);
            currentSolution = localChoice(potentialSolutions);
            ++iteration;
            if (iteration%500 == 0) 
                System.out.println("Iteration : " + iteration);
            current_time = System.currentTimeMillis() + 10000;
        } while((iteration<MAX_ITERATION) && (current_time < end_time));

        plans = currentSolution.generatePlans(this.vehicles, true);
        return plans;
    }


    private Solution selectInitialSolution() {
        // return a plan that is valid
        // udpate the lists
        Solution initialSolution = new Solution();

        HashMap<Task, Integer> vehicles = new HashMap<Task, Integer>();
        HashMap<Task, Task> nextTaskForTask = new HashMap<Task, Task>();
        HashMap<Integer, Task> nextTaskForVehicle = new HashMap<Integer, Task>();
        HashMap<Task, Integer> pickupTimes = new HashMap<Task, Integer>();
        HashMap<Task, Integer> deliveryTimes = new HashMap<Task, Integer>();
        
        int indexVehicle;
        int nbVehicles = this.vehicles.size();
        List<Vehicle> sortedVehicles = sortByCapacity(this.vehicles);
        Task lastTask = null;
        int currentTimeStep = 0;

        switch (initialization) {
            case SLAVE:
                for (Task task: tasks) {
                    Vehicle vehicle = sortedVehicles.get(0); 
                    vehicles.put(task, vehicle.id()); 
                    if (lastTask != null) {
                        nextTaskForTask.put(lastTask, task);
                    } else {
                        nextTaskForVehicle.put(vehicle.id(), task);
                    }
                    pickupTimes.put(task, currentTimeStep);
                    deliveryTimes.put(task, currentTimeStep+1);
                    lastTask = task;
                    currentTimeStep += 2;
                }
                nextTaskForTask.put(lastTask, null); 
                break;

            case SEQUENTIAL:
                double totalWeight = 0.0; 
                indexVehicle = 0; 
                for (Task task: tasks) {
                    Vehicle vehicle = sortedVehicles.get(indexVehicle); 

                    if ((vehicle.capacity() >= task.weight + totalWeight) || (indexVehicle == nbVehicles-1)) {
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
                        totalWeight = task.weight;
                        lastTask = task;
                        currentTimeStep = 2;
                        while (!(sortedVehicles.get(indexVehicle).capacity() >= task.weight)) {
                            indexVehicle++; 
                        }
                        nextTaskForVehicle.put(sortedVehicles.get(indexVehicle).id(), task);
                        vehicles.put(task, sortedVehicles.get(indexVehicle).id()); 
                        pickupTimes.put(task, 0);
                        deliveryTimes.put(task, 1);
                    }
                }
                nextTaskForTask.put(lastTask, null);
                break;

            case DISTRIBUTED:
                // Set vehicles
                Task[] lastTaskForVehicle = new Task[nbVehicles];
                int[] currentTimeStepForVehicle = new int[nbVehicles];
                for (int i=0; i<nbVehicles; i++) {
                    lastTaskForVehicle[i] = null;
                    currentTimeStepForVehicle[i] = 0;
                }
                indexVehicle = 0;
                for (Task task: tasks) {
                    int vehiclesTested = 0;
                    while (this.vehicles.get(indexVehicle).capacity() < task.weight) {
                        indexVehicle = (indexVehicle + 1)%nbVehicles;
                        vehiclesTested++;
                        if (vehiclesTested >= nbVehicles) { throw new IllegalArgumentException("Cannot handle task with weight higher than all vehicles' capacity.");}
                    }
                    Vehicle vehicle = this.vehicles.get(indexVehicle);

                    vehicles.put(task, vehicle.id()); 
                    if (lastTaskForVehicle[indexVehicle] != null) {
                        nextTaskForTask.put(lastTaskForVehicle[indexVehicle], task);
                    } else {
                        nextTaskForVehicle.put(vehicle.id(), task);
                    }
                    pickupTimes.put(task, currentTimeStepForVehicle[indexVehicle]);
                    deliveryTimes.put(task, currentTimeStepForVehicle[indexVehicle]+1);

                    lastTaskForVehicle[indexVehicle] = task;
                    currentTimeStepForVehicle[indexVehicle] += 2;

                    indexVehicle = (indexVehicle + 1)%nbVehicles;
                }
                
                for (Task lastTask_ : lastTaskForVehicle) {
                    if(lastTask_ != null) {
                        nextTaskForTask.put(lastTask_, null);
                    }
                }
                break;

            default: throw new IllegalArgumentException("Should not happen");
        }
        
        initialSolution.vehicles = vehicles; 
        initialSolution.nextTaskForVehicle = nextTaskForVehicle; 
        initialSolution.deliveryTimes = deliveryTimes; 
        initialSolution.pickupTimes = pickupTimes;         
        initialSolution.nextTaskForTask = nextTaskForTask; 

        return initialSolution;
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

    // Sort List of costs by decreasing order and return index if sorted list
	private int[] sortByCost(List<Double> costsForSolution) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, costsForSolution.size())
			.boxed().sorted((i, j) -> Double.valueOf(costsForSolution.get(i)).compareTo(Double.valueOf(costsForSolution.get(j))))
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
        
        int lengthI = getNumberTasks(solution, vehicleI); 

        /*Task task = solution.nextTaskForVehicle.get(vehicleI.id());
        int length = 0;
        do {
            task = solution.nextTaskForTask.get(task);
            length++;
        } while(task != null);*/
       
        
        for (Vehicle vehicleJ : vehicles) {
            if (!vehicleJ.equals(vehicleI)) {
                for (int tIdx=0; tIdx<lengthI; tIdx++) {
                //if (task.weight <= vehicleJ.capacity()) {
                    Solution temp = changingVehicle(solution, vehicleI, vehicleJ, tIdx);
                    if (temp.isValid(tasks, vehicles)) { neighours.add(temp);}
                    int lengthJ = getNumberTasks(temp, vehicleJ); 
                    for (int tIdx2=1; tIdx2<lengthJ-1; tIdx2++) {
                        List<Solution> solutions = changingTaskOrder(temp, vehicleJ, 0, tIdx2);
                        neighours.addAll(solutions);
                    }
                }
            }
        }
        
        if (lengthI > 2) {
            for (int tIdx1=0; tIdx1<lengthI-1; tIdx1++) {
                for (int tIdx2=tIdx1+1; tIdx2<lengthI; tIdx2++) {
                    List<Solution> solutions = changingTaskOrder(solution, vehicleI, tIdx1, tIdx2);
                    neighours.addAll(solutions);
                }
            }
        }
        
    	return neighours;
    }

    private int getNumberTasks(Solution solution, Vehicle vehicle){
        Task task = solution.nextTaskForVehicle.get(vehicle.id());
        
        int length = 0;
        while (task != null) {
            task = solution.nextTaskForTask.get(task);
            length++;
        }

        return length;
    }
    
    private Solution changingVehicle(Solution solution, Vehicle v1, Vehicle v2, int tIdx) {
        Solution newSolution = new Solution(solution);

        Task prevMovedTask = newSolution.nextTaskForVehicle.get(v1.id());
        Task movedTask = newSolution.nextTaskForTask.get(prevMovedTask);
        Task postMovedTask = null;

        if (movedTask == null) {
            movedTask = prevMovedTask;
            prevMovedTask = null;
        } else if (tIdx == 0) {
            movedTask = prevMovedTask;
            prevMovedTask = null;
            postMovedTask = newSolution.nextTaskForTask.get(movedTask);
        } else {
            int count = 1;
            while (count < tIdx) {
                prevMovedTask = movedTask;
                movedTask = newSolution.nextTaskForTask.get(prevMovedTask);
                count++;
            }
            postMovedTask = newSolution.nextTaskForTask.get(movedTask);
        }

        if (movedTask.weight > v2.capacity()) {
            // return unvalid solution 
            return new Solution();
        }

        if(tIdx==0) {
            newSolution.nextTaskForVehicle.put(v1.id(), postMovedTask);
        }
        else {
            newSolution.nextTaskForTask.put(prevMovedTask, postMovedTask); 
        }
        // Put it first in v2
        newSolution.nextTaskForTask.put(movedTask, newSolution.nextTaskForVehicle.get(v2.id())); 
        newSolution.nextTaskForVehicle.put(v2.id(), movedTask);

       // Corresponding delivery time 
       Integer movedTaskPickupTimeInV1 = newSolution.pickupTimes.get(movedTask); 
       Integer movedTaskDeliveryTimeInV1 = newSolution.deliveryTimes.get(movedTask); 

        // Solution where deliveryTime stays the same for both tasks 
        for (Entry<Task, Integer> entry: newSolution.deliveryTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == v1.id()) {
                if (entry.getValue() < movedTaskDeliveryTimeInV1 &&
                    entry.getValue() >= movedTaskPickupTimeInV1) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-1); 
                }
                else if (entry.getValue() >= movedTaskDeliveryTimeInV1) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-2); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == v2.id()) {
                newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+2); 
            }
        }

        for (Entry<Task, Integer> entry: newSolution.pickupTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == v1.id()) {
                if (entry.getValue() < movedTaskDeliveryTimeInV1 &&
                    entry.getValue() > movedTaskPickupTimeInV1) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-1); 
                }
                else if (entry.getValue() >= movedTaskDeliveryTimeInV1) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-2); 
                }
            }
            if (newSolution.vehicles.get(entry.getKey()) == v2.id()) {
                newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+2); 
            }
        }

        // add new delivery and pickup 
        newSolution.pickupTimes.put(movedTask, 0);
        newSolution.deliveryTimes.put(movedTask, 1); 
        newSolution.vehicles.put(movedTask, v2.id());

        /*System.out.println(tIdx);
        System.out.println(newSolution.nextTaskForVehicle);
        System.out.println(newSolution.nextTaskForTask);
        System.out.println(newSolution.pickupTimes);
        System.out.println(newSolution.deliveryTimes);
        System.out.println(newSolution.vehicles);*/

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
        // Create another one 
        Solution newSolutionTemp =  new Solution(newSolution);
        newSolutionTemp.pickupTimes.put(task2, task1PickupTime);
        newSolutionTemp.deliveryTimes.put(task2, task1DeliveryTime);
        newSolutionTemp.pickupTimes.put(task1, task2PickupTime);
        newSolutionTemp.deliveryTimes.put(task1, task2DeliveryTime);
        if (newSolutionTemp.isValid(tasks, vehicles)) { newSolutions.add(newSolutionTemp);}
        
        // Create new solution by switching 
        Random random = new Random();
        int maxTime = Math.max(task1DeliveryTime, task2DeliveryTime);
        int randomTask1DeliveryTime = random.nextInt(maxTime - task1PickupTime) + task1PickupTime + 1;
        int randomTask2DeliveryTime = random.nextInt(maxTime - task2PickupTime) + task2PickupTime + 1;

        // Swap the pickup times 
        for (Entry<Task, Integer> entry: newSolution.deliveryTimes.entrySet()) {
            // if task for the vehicle 
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask1DeliveryTime < task1DeliveryTime && 
                    entry.getValue() >= randomTask1DeliveryTime &&
                    entry.getValue() < task1DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+1); 
                    if(entry.getValue() == randomTask2DeliveryTime) {randomTask2DeliveryTime++;}
                } else if (randomTask1DeliveryTime > task1DeliveryTime && 
                    entry.getValue() <= randomTask1DeliveryTime &&
                    entry.getValue() > task1DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-1); 
                    if(entry.getValue() == randomTask2DeliveryTime) {randomTask2DeliveryTime--;}
                }
            }
        }

        for (Entry<Task, Integer> entry: newSolution.deliveryTimes.entrySet()) {
            // if task for the vehicle 
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask2DeliveryTime < task2DeliveryTime && 
                    entry.getValue() >= randomTask2DeliveryTime &&
                    entry.getValue() < task2DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()+1); 
                    if(entry.getValue() == randomTask1DeliveryTime) {randomTask1DeliveryTime++;}
                } else if (randomTask2DeliveryTime > task2DeliveryTime && 
                    entry.getValue() <= randomTask2DeliveryTime &&
                    entry.getValue() > task2DeliveryTime) {
                    newSolution.deliveryTimes.put(entry.getKey(), entry.getValue()-1); 
                    if(entry.getValue() == randomTask1DeliveryTime) {randomTask1DeliveryTime--;}
                }
            }
        }
        newSolution.deliveryTimes.put(task2, randomTask1DeliveryTime);
        newSolution.deliveryTimes.put(task1, randomTask2DeliveryTime);

        for (Entry<Task, Integer> entry: newSolution.pickupTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask1DeliveryTime < task1DeliveryTime && 
                    entry.getValue() >= randomTask1DeliveryTime &&
                    entry.getValue() < task1DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+1);
                    if(entry.getValue() == randomTask2DeliveryTime) {randomTask2DeliveryTime++;} 
                } else if (randomTask1DeliveryTime > task1DeliveryTime && 
                    entry.getValue() <= randomTask1DeliveryTime &&
                    entry.getValue() > task1DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-1); 
                    if(entry.getValue() == randomTask2DeliveryTime) {randomTask2DeliveryTime--;}
                }
            }
        }

        for (Entry<Task, Integer> entry: newSolution.pickupTimes.entrySet()) {
            if (newSolution.vehicles.get(entry.getKey()) == vehicle.id()) {
                if (randomTask2DeliveryTime < task2DeliveryTime && 
                    entry.getValue() >= randomTask2DeliveryTime &&
                    entry.getValue() < task2DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()+1); 
                    if(entry.getValue() == randomTask1DeliveryTime) {randomTask1DeliveryTime++;}
                } else if (randomTask2DeliveryTime > task2DeliveryTime && 
                    entry.getValue() <= randomTask2DeliveryTime &&
                    entry.getValue() > task2DeliveryTime) {
                    newSolution.pickupTimes.put(entry.getKey(), entry.getValue()-1); 
                    if(entry.getValue() == randomTask1DeliveryTime) {randomTask1DeliveryTime--;}
                }
            }
        }
        newSolution.pickupTimes.put(task2, task1PickupTime);
        newSolution.pickupTimes.put(task1, task2PickupTime);
        
        
        if (newSolution.isValid(tasks, vehicles)) { newSolutions.add(newSolution);}
        return newSolutions;
    }

    private Solution localChoice(List<Solution> potentialSolutions) {
        List<Double> costsForSolutions = new ArrayList<Double>(); 
        for (Solution solution: potentialSolutions) {
            List<Plan> plansForSolution = solution.generatePlans(vehicles, false);
            costsForSolutions.add(computeCost(plansForSolution)); 
        }
        
        int[] sortedIndices = sortByCost(costsForSolutions);

        if ((sortedIndices.length > 0) && ((new Random()).nextDouble() <= PROBABILITY_UPDATE_SOLUTION)) {
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

}
