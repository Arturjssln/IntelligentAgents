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
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedAgent implements CentralizedBehavior {

	// Different algorithms implemented
	enum Algorithm { SLS, NAIVE};

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup; // TODO: a quoi ça sert ça ?????
    private long timeout_plan;

    private Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution distribution, Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        String algorithmName = agent.readProperty("algorithm", String.class, "SLS");

        // Throws IllegalArgumentException if algorithm is unknown
		this.algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
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
                plans = computeNaivePlans(vehicles, tasks);
                break;
            case SLS:
                SLSAlgo sls = new SLSAlgo(vehicles);
                plans = sls.computePlans(tasks, this.timeout_plan, time_start);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        long time_end = System.currentTimeMillis();
        long duration = (time_end - time_start) / 1000.0;
        System.out.println("The plan was generated in " + duration + " seconds using " + algorithm.toString() + " alogirthm.");

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
        return plans
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
