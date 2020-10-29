package template;


public class SLSAlgo {

    // To solve discrete constraint optimization problem (COP)

    // probably to put in a specific class Solution or sthg
    private TaskSet nextTasks; // one variable for every existing task, and one variable for every existing vehicle
    private List<int> times; // one variable for each task : order of delivery
    private List<vehicle> vehicles; // one variable for each task : which vehicle for which task ?

    


    public List<Plan> computePlans() {
        List<Plan> plans = new List<Plan> (); 

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

        return new List<Plan>; 
    }

 
}