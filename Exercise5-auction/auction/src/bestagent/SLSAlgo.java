package bestagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.stream.IntStream;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;


public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    private Solution currentSolution;
    private List<FashionVehicle> vehicles;
    private List<Task> tasks;
    private Solution bestSolutionEver; 

    final int MAX_ITERATION = 1000;
    final double PROBABILITY_UPDATE_SOLUTION = 0.4; 
    final long timeToComputePlan = 1;

	// Constructor
	public SLSAlgo(List<FashionVehicle> vehicles, List<Task> tasks) {
        this.vehicles = vehicles;
        this.tasks = tasks;
        this.currentSolution = selectInitialSolution();
        this.bestSolutionEver = currentSolution;
	}


    public List<Plan> computePlans(long end_time) {        
        long currentTime = System.currentTimeMillis();
        if (tasks.size()==0) {
            List<Plan> plans =  new ArrayList<Plan>();
            for(int i=0;i<vehicles.size();++i) {
                plans.add(Plan.EMPTY);
            }
            return plans;
        }

        // Slave initialisation 
        currentSolution = selectInitialSolution();
        List<Solution> potentialSolutions = new ArrayList<Solution> ();
        
        int iteration = 0;
        long timeForOneIteration = 0;
        long startTime = System.currentTimeMillis(); 
        do {
            potentialSolutions = generateNeighbours(currentSolution);
            currentSolution = localChoice(potentialSolutions);
            ++iteration; 
            currentTime = System.currentTimeMillis(); 
            timeForOneIteration = (currentTime - startTime)/((long)iteration) + 1;
        } while((iteration<MAX_ITERATION)&& (currentTime + timeForOneIteration + timeToComputePlan + 10 < end_time));

        bestSolutionEver.computePlans(this.vehicles, false); //true
        return bestSolutionEver.plans;
    }

    public double computeCostBestSolution(long end_time) {
        return computeCost(computePlans(end_time));
    }

    public List<Plan> getBestPlansEver() {
        return bestSolutionEver.plans; 
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
        
        FashionVehicle maxCapacityVehicle = getMaxCapacityVehicle(this.vehicles);
        Task lastTask = null;
        
        int currentTimeStep = 0;
        for (Task task: tasks) {
            Vehicle vehicle = maxCapacityVehicle;
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

        
        initialSolution.vehicles = vehicles; 
        initialSolution.nextTaskForVehicle = nextTaskForVehicle; 
        initialSolution.deliveryTimes = deliveryTimes; 
        initialSolution.pickupTimes = pickupTimes;         
        initialSolution.nextTaskForTask = nextTaskForTask; 

        return initialSolution;
    }

    // Sort List of Vehicles by increasing order of capacity
	private FashionVehicle getMaxCapacityVehicle(List<FashionVehicle> vehicles) {
        // Get indices of the sorted List 
        int[] sortedIndices = IntStream.range(0, vehicles.size())
			.boxed().sorted((i, j) -> Double.valueOf(vehicles.get(j).capacity()).compareTo(Double.valueOf(vehicles.get(i).capacity())))
            .mapToInt(ele -> ele).toArray();
        
        return vehicles.get(sortedIndices[0]);
        //Reorder list
        // LinkedList<Vehicle> sortedVehicles = new LinkedList<Vehicle>();
		// for (int index : sortedIndices) {
		// 	sortedVehicles.add(sortedVehicles);
        // }
		// return sortedVehicles; 
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
            solution.computePlans(vehicles, false);
            costsForSolutions.add(computeCost(solution.plans)); 
        }
        
        int[] sortedIndices = sortByCost(costsForSolutions);
        
        Solution bestNeighbourSolution = potentialSolutions.get(sortedIndices[0]);
        // Save the best neighbour solution if best so far 
        bestSolutionEver.computePlans(vehicles, false);
        bestSolutionEver = (costsForSolutions.get(sortedIndices[0]) < computeCost(bestSolutionEver.plans))
                ? bestNeighbourSolution : currentSolution; 

        if ((sortedIndices.length > 0) && ((new Random()).nextDouble() <= PROBABILITY_UPDATE_SOLUTION)) {
            return bestNeighbourSolution;
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
