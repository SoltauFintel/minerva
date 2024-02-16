package minerva.task;

import java.util.List;
import java.util.stream.Collectors;

import minerva.model.UserSO;
import minerva.seite.CommentWithSeite;

public class CommentsSupplier implements TasksSupplier {

    @Override
    public List<Task> getTasks(UserSO user, String branch, String login) {
        return toTasks(user.getUndoneCommentsToBeCompletedByMe(branch, login), branch);
    }

    @Override
    public List<Task> getTasksCreatedByMe(UserSO user, String branch, String login) {
        return toTasks(user.getUndoneCommentsCreatedByMe(branch, login), branch);
    }
    
    private List<Task> toTasks(List<CommentWithSeite> comments, String branch) {
        return comments.stream()
                .map(comment -> new CommentTask(comment, branch))
                .collect(Collectors.toList());
    }
}
