package template;

import java.util.ArrayList;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    // List<Solution> PotentialSolutions; // TODO: faudrait l'initialiser  
    List<Vehicle> vehicles;

    final int MAX_ITERATION = 10000; 

	// Constructor
	public SLSAlgo(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
	}


    public List<Plan> computePlans(TaskSet tasks, long time_out, long start_time) {
        List<Plan> plans = new ArrayList<Plan> (); 
        Solution optimalSolution = new Solution(); 

        optimalSolution = selectInitialSolution();
        List<Solution> potentialSolutions = new ArrayList<Solution> (); 

        int iteration = 0; 
        do {
            potentialSolutions = generateNeighbours(optimalSolution); 
            optimalSolution = localChoice(potentialSolutions); 
            ++iteration; 
        } while(iteration<MAX_ITERATION); 
        
        plans = optimalSolution.generatePlans(); 

        // TODO: verify that (current_time < start_time + time_out) in loop
        long current_time = System.currentTimeMillis();
        // need to be of size #vehicles


        return plans;
    }


    private Solution selectInitialSolution() {
        // return a plan that is valid 
        // udpate the lists 

        return new Solution(); 
    }

    private List<Solution> generateNeighbours(Solution solution) {
        // changing vehicle for the task
        // changing task order 
        // others ? ...
    	
    	return new ArrayList<Solution>(); 
    }


    private Solution localChoice(List<Solution> potentialSolutions) {
        double probability = 0.3; 

        return new Solution(); 
    }
    private void updateTime(Solution solution) {

    }
 
}