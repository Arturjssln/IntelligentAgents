package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import logist.LogistSettings;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import template.SLSAlgo;

/**
 * An auction agent that distributes all tasks to its vehicles and
 * handles them in an optimal way in order to minimize the cost of the company plan.
 *
 */
@SuppressWarnings("unused")
public class CentralizedAgent implements CentralizedBehavior {

	// Different algorithms implemented
    enum Algorithm {SLS, NAIVE};
    enum Initialization {DISTRIBUTED, SEQUENTIAL, SLAVE}; 

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup; 
    private long timeout_plan;
    
    private Algorithm algorithm;
    private Initialization initialization; 

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        // This code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        String algorithmName = agent.readProperty("algorithm", String.class, "SLS");
        String initializationName = agent.readProperty("initialization", String.class, "DISTRIBUTED");


        // Throws IllegalArgumentException if algorithm is unknown
        this.algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
        this.initialization = Initialization.valueOf(initializationName.toUpperCase()); 
        System.out.println("Algorithm used : " + algorithm.toString());
        System.out.println("Initialization used : " + initialization.toString());
        // The setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // The plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
        
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        // Compute the plans for each vehicle with the selected algorithm.
        List<Plan> plans = new ArrayList<Plan>();
		switch (algorithm) {
            case NAIVE:
                /* A very simple auction agent that assigns all tasks to its first vehicle and handles them sequentially. */
                plans = computeNaivePlans(vehicles, tasks);
                break;
            case SLS:
                SLSAlgo sls = new SLSAlgo(vehicles, tasks, initialization); 
                /* An auction agent that distributes all tasks to its vehicles and
                * handles them in an optimal way in order to minimize the cost of the company plan. */
                plans = sls.computePlans(tasks, time_start+this.timeout_plan);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }

        long time_end = System.currentTimeMillis();
        double duration = (time_end - time_start) / 1000.0;
        System.out.println("The plan was generated in " + duration + " seconds.");
        
        return plans;
    }

    private List<Plan> computeNaivePlans(List<Vehicle> vehicles, TaskSet tasks) {
        // First vehicle get all the tasks, other vehicles can rest
        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);

        List<Plan> plans = new ArrayList<Plan>();
        plans.add(planVehicle1);
        while (plans.size() < vehicles.size()) {
            plans.add(Plan.EMPTY);
        }
        return plans;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}
