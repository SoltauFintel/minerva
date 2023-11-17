package minerva.task;

import java.util.List;
import java.util.stream.Collectors;

import minerva.model.UserSO;

public class NotesSupplier implements TasksSupplier {

	@Override
	public List<Task> getTasks(UserSO user, String branch, String login) {
		return user.getNotes(branch, login)
				.stream()
				.map(note -> new NoteTask(note, branch))
				.collect(Collectors.toList());
	}
}
