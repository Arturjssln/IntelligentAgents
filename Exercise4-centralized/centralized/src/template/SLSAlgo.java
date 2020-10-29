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

    // probably to put in a specific class Solution or sthg
    private TaskSet nextTasks; // one variable for every existing task, and one variable for every existing vehicle
    private List<Integer> times; // one variable for each task : order of delivery
    private List<Vehicle> vehicles; // one variable for each task : which vehicle for which task ?

    


    public List<Plan> computePlans() {
        List<Plan> plans = new ArrayList<Plan> (); 

        plans = selectInitialSolution();
        

        // need to be of size #vehicles


        return plans;
    }

    private boolean constraintsChecker() {
        for (Task task: nextTasks) {

        }

        return true; 
    }

    private List<Plan> selectInitialSolution() {
        // return a plan 
        // udpate the 

        return new ArrayList<Plan>(); 
    }

 
}