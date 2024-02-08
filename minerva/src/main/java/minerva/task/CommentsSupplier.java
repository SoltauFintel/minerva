package minerva.task;

import java.util.List;
import java.util.stream.Collectors;

import minerva.model.UserSO;

public class CommentsSupplier implements TasksSupplier {

	@Override
	public List<Task> getTasks(UserSO user, String branch, String login) {
		return user.getComments(branch, login)
				.stream()
				.map(comment -> new CommentTask(comment, branch))
				.collect(Collectors.toList());
	}
}
