package template;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskSet;

import template.Algo;

public abstract class Algo {

    public abstract Plan computePlan(Vehicle vehicle, TaskSet tasks);

}