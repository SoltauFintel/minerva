package minerva.task;

import static gitper.base.StringService.makeClickableLinks;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import gitper.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class TasksCreatedByMePage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
        String login = ctx.queryParam("login");
        boolean me = login == null || user.getLogin().equals(login);
        Logger.info(user.getLogin() + " | " + (me ? "Tasks created by me" : "Tasks created by " + login));

        List<Task> tasks = new TaskService().getTasksCreatedByMe(user, branch, login);

        header(n("tasksCreatedByMe"));
        put("login", esc(UserAccess.login2RealName(StringService.isNullOrEmpty(login) ? user.getLogin() : login)));
        putInt("n", tasks.size());
        put("hasTasks", !tasks.isEmpty());
        DataList list = list("tasks");
        for (Task task : tasks) {
            DataMap map = list.add();
            map.put("id", task.getId());
            map.put("me", task.getLogin().equals(login));
            map.put("person", esc(UserAccess.login2RealName(task.getPerson())));
            map.put("date", esc(task.getDateTime()));
            String text = StringService.onlyBody(task.getText());
            map.put("text1", makeClickableLinks(text));
            map.put("completeText", makeClickableLinks(text));
            map.put("link", task.getLink()); // Kommentar anzeigen Link
            map.put("viewTask", esc(getShowCommentLabel(task))); // Kommentar anzeigen Label
            map.put("parentLink", task.getParentLink());
            map.put("parentTitle", esc(task.getParentTitle()));
            map.put("color", esc(task.getColor()));
        }
    }

    private String getShowCommentLabel(Task task) {
        String typeName = n(task.getTypeName());
        return n("viewTask").replace("$t", typeName);
    }
}
