package minerva.task;

import java.util.ArrayList;
import java.util.List;

import minerva.model.UserSO;

public class TaskService {
	public static List<TasksSupplier> tasksSuppliers = new ArrayList<>();
	
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
}
