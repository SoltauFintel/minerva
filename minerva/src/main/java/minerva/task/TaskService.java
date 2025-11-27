package minerva.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.model.UserSO;

public class TaskService {
    public static List<TasksSupplier> tasksSuppliers = new ArrayList<>();
    private static final Map<String, Integer> openMasterTasks = new HashMap<>();
    
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
        update(user, new TaskService().getNumberOfTasks(user));
    }
    
    public static void update(UserSO user, int omt) {
        openMasterTasks.put(user.getLogin(), Integer.valueOf(omt));
    }
    
    public static int get(UserSO user) {
        if (user != null) {
            Integer i = openMasterTasks.get(user.getLogin());
            // null check for i is necessary!
            if (i != null) {
                return i.intValue();
            }
        }
        return 0;
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
