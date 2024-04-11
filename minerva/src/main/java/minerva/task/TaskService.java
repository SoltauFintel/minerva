package minerva.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.model.UserSO;

public class TaskService {
    public static List<TasksSupplier> tasksSuppliers = new ArrayList<>();
    public static final Map<String, Integer> openMasterTasks = new HashMap<>();
    
    static {
        tasksSuppliers.add(new CommentsSupplier());
    }

    public List<Task> getTasks(UserSO user, String branch, String login) {
        List<Task> tasks = new ArrayList<>();
        for (TasksSupplier tasksSupplier : tasksSuppliers) {
            tasks.addAll(tasksSupplier.getTasks(user, branch, login));
        }
        tasks.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime())); // newest first
        return tasks;
    }
    
    public int getNumberOfTasks(UserSO user) {
        int n = 0;
        for (TasksSupplier tasksSupplier : tasksSuppliers) {
            n += tasksSupplier.getTasks(user, "master", null).stream()
                    .filter(i -> !TaskPriority.HIDE.equals(user.getTaskPriority(i.getId())))
                    .count();
        }
        return n;
    }
    
    public static void update(UserSO user) {
        int omt = new TaskService().getNumberOfTasks(user);
        openMasterTasks.put(user.getLogin(), Integer.valueOf(omt));
    }

    public List<Task> getTasksCreatedByMe(UserSO user, String branch, String login) {
        List<Task> tasks = new ArrayList<>();
        for (TasksSupplier tasksSupplier : tasksSuppliers) {
            tasks.addAll(tasksSupplier.getTasksCreatedByMe(user, branch, login));
        }
        tasks.sort((a, b) -> a.getDateTime().compareTo(b.getDateTime())); // oldest first
        return tasks;
    }
}
