package matthewtestagent;

import logist.task.Task;

public class CSPTask {
	public Task task;
	public int timeInTrunk=0;
	
	public CSPTask(Task task, int timeInTrunk) {
		this.task = task;
		this.timeInTrunk = timeInTrunk;
	}
	
}
