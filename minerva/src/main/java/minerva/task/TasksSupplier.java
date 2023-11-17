package minerva.task;

import java.util.List;

import minerva.model.UserSO;

public interface TasksSupplier {

	List<Task> getTasks(UserSO user, String branch, String login);
}
