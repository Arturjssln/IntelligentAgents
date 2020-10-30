package template;

import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;

import java.util.ArrayList;
import java.util.List;

import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class Solution {

    // To solve discrete constraint optimization problem (COP)

    private TaskSet nextTasks; // one variable for every existing task, and one variable for every existing vehicle
    private TaskSet nextTaskVehicle; 
    private List<Integer> times; // one variable for each task : order of delivery
    private List<Vehicle> vehicles; // one variable for each task : which vehicle for which task ?
   
    public void setTimeStep(int time, int index) {
        this.times.set(index, time); 
    }


    public List<Plan> generatePlans() {

        // generate plan based on the attributes of the solution 

        return new ArrayList<Plan> (); 
    }

    
    public boolean isValid(TaskSet tasks, List<Vehicle> vehicles) {
        // Respects the constraints 
        boolean isValid = true;
        
        // if the attributes are of the good size 
        if (!attributeSizeValide(tasks, vehicles)) {
            isValid = false;  
        } /*else {
            for (int i=0; i< tasks.size(); i++) {
                if (tasks.get(i) == nextTasks.get(i)) {
                    isValid = false; 
                    break; 
                }
            }
            for (Task task: tasks) {
                for (int i=tasks.size()-1; i<nextTasks.size(); i++) {
                    for (int j=0)
                    if (nextTasks.get(i) == task) {
                        if (times.get()) {

                        }
                    }
                }
            }
        }*/
        
        return isValid; 
        
    
    }

    private boolean attributeSizeValide(TaskSet tasks, List<Vehicle> vehicles) {
        boolean isRightSize = ((nextTasks.size() == (tasks.size()+vehicles.size()))
                            && (times.size() == tasks.size()) 
                            && (this.vehicles.size() == vehicles.size())); 
        return isRightSize; 
    }
    
}