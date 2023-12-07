package minerva.task;

import java.util.List;

import minerva.model.UserSO;

public interface TasksSupplier {

    /**
     * Loads all tasks out of one source
     * 
     * @param user object for accessing data
     * @param branch load tasks for that branch, e.g. "master"
     * @param login null if given user.login should be used
     * @return tasks of given branch and given user-or-login
     */
	List<Task> getTasks(UserSO user, String branch, String login);
}
