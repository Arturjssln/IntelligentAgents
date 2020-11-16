package matthewtestagent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import logist.task.Task;
import logist.task.TaskSet;

public class CSPSolution {
	// Do we actually need this split into tasks for CustomVehicle and for action?
		HashMap<CustomVehicle, Task> TasksForCustomVehicleAction = new HashMap<CustomVehicle, Task>();
		HashMap<Task, Task> TasksForTask = new HashMap<Task, Task>();
		HashMap<Task, Integer> time = new HashMap<Task, Integer>();
		HashMap<Task, Integer> timeInTrunk = new HashMap<Task, Integer>();
		HashMap<Task, CustomVehicle> CustomVehicle = new HashMap<Task, CustomVehicle>();

		
		public CSPSolution(List<CustomVehicle> CustomVehicles, List<Task> deliveryTasks) {
			// initialize HashMaps
			for(CustomVehicle v: CustomVehicles) {
				TasksForCustomVehicleAction.put(v, null);
			}
			for(Task task: deliveryTasks) {
					TasksForTask.put(task, null);
					time.put(task, 1);
					timeInTrunk.put(task, 1);
					CustomVehicle.put(task, null);
			}
		}
		
		// Copy constructor
		public CSPSolution(CSPSolution differentSolution) {
			this.TasksForCustomVehicleAction = new HashMap<CustomVehicle, Task>(differentSolution.TasksForCustomVehicleAction);
			this.TasksForTask = new HashMap<Task, Task>(differentSolution.TasksForTask);
			this.time = new HashMap<Task, Integer>(differentSolution.time);
			this.CustomVehicle = new HashMap<Task, CustomVehicle>(differentSolution.CustomVehicle);
			this.timeInTrunk = new HashMap<Task, Integer>(differentSolution.timeInTrunk);
		}
		
		public List<CustomVehicle> getInvolvedCustomVehicles(){
			List<CustomVehicle> CustomVehiclesInvolved = new ArrayList<CustomVehicle>();
			
			for (CustomVehicle keyCustomVehicle: TasksForCustomVehicleAction.keySet()) {
				Task t = TasksForCustomVehicleAction.get(keyCustomVehicle);
				if(t != null && !CustomVehiclesInvolved.contains(keyCustomVehicle)) {
					CustomVehiclesInvolved.add(keyCustomVehicle);
				}
			}
			return CustomVehiclesInvolved;
		}
		
		public List<Task> getAllTasks(){
			List<Task> Tasks = new ArrayList<Task>();
			
			for (Task key: CustomVehicle.keySet()) {
				if(key != null) {
					Tasks.add(key);
				}
			}
			
			return Tasks;
		}
		
		public Task getNextTask(CustomVehicle v) {
			return TasksForCustomVehicleAction.get(v);
		}
		
		public void setNextTask(CustomVehicle v, Task Task) {
			TasksForCustomVehicleAction.put(v, Task);
		}
		
		public Task	getNextTask(Task Task) {
			return TasksForTask.get(Task);
		}
		
		public void setNextTask(Task key, Task Task) {
			TasksForTask.put(key, Task);
		}
		
		public int	getTime(Task Task) {
			return time.get(Task);
		}
		
		public void setTime(Task key, int t) {
			time.put(key, t);
		}
		
		public CustomVehicle	getCustomVehicle(Task Task) {
			return CustomVehicle.get(Task);
		}
		
		public void setCustomVehicle(Task key, CustomVehicle v) {
			CustomVehicle.put(key, v);
		}

		
		public int	getTimeInTrunk(Task Task) {
			return timeInTrunk.get(Task);
		}
		
		public void setTimeInTrunk(Task key, int t) {
			timeInTrunk.put(key, t);
		}
				
}
