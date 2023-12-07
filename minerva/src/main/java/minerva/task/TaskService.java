package minerva.task;

import java.util.ArrayList;
import java.util.List;

import minerva.model.UserSO;

public class TaskService {
	public static List<TasksSupplier> tasksSuppliers = new ArrayList<>();
	public static int openMasterTasks = 0;
	
	static {
		tasksSuppliers.add(new NotesSupplier());
	}

	public List<Task> getTasks(UserSO user, String branch, String login) {
        List<Task> tasks = new ArrayList<>();
        for (TasksSupplier tasksSupplier : tasksSuppliers) {
        	tasks.addAll(tasksSupplier.getTasks(user, branch, login));
        }
        tasks.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));
        return tasks;
	}
	
	public int getNumberOfTasks(UserSO user) {
	    int n = 0;
        for (TasksSupplier tasksSupplier : tasksSuppliers) {
            n += tasksSupplier.getTasks(user, "master", null).size();
        }
        return n;
	}
	
	public static void update(UserSO user) {
	    openMasterTasks = new TaskService().getNumberOfTasks(user);
	}
}
