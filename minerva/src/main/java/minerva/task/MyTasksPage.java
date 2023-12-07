package minerva.task;

import static minerva.base.StringService.cutOutsideLinks;
import static minerva.base.StringService.makeClickableLinks;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.MinervaPageInitializer;
import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {
	
    @Override
    protected void execute() {
    	String login = ctx.queryParam("login");
    	boolean me = login == null || user.getLogin().equals(login);
		Logger.info(user.getLogin() + " | " + (me ? "My tasks" : "All tasks for " + login));
        
        List<Task> tasks = new TaskService().getTasks(user, branch, login);
        if ("master".equals(branch) && me) {
            TaskService.openMasterTasks = tasks.size();
            MinervaPageInitializer.updateOpenMasterTasks(this);
        }
        
        header(n("myTasks"));
        put("login", esc(UserAccess.login2RealName(StringService.isNullOrEmpty(login) ? user.getLogin() : login)));
		fill(tasks, branch, model, user.getLogin());
        putSize("n", tasks);
        put("hasTasks", !tasks.isEmpty());
        put("showTaskButtons", me);
    }
    
    private void fill(List<Task> tasks, String branch, DataMap model, String login) {
        DataList list = model.list("tasks");
        fill2(tasks, branch, login, TaskPriority.TOP, list);
        fill2(tasks, branch, login, TaskPriority.NORMAL, list);
        fill2(tasks, branch, login, TaskPriority.HIDE, list);
    }

    private void fill2(List<Task> tasks, String branch, String login, TaskPriority showOnlyPrio, DataList list) {
        for (Task task : tasks) {
            if (!user.getTaskPriority(task.getId()).equals(showOnlyPrio)) {
                continue;
            }
            DataMap map = list.add();
            map.put("id", task.getId());
            map.put("me", task.getLogin().equals(login));
            map.put("user", esc(UserAccess.login2RealName(task.getLogin())));
            map.put("date", esc(task.getDateTime()));
            String text = task.getText();
            if (text.length() > 113) {
                map.put("text1", makeClickableLinks(esc(cutOutsideLinks(text, 110))));
                map.put("hasMoreText", true);
            } else {
                map.put("text1", makeClickableLinks(esc(text)));
                map.put("hasMoreText", false);
            }
            map.put("completeText", makeClickableLinks(esc(text)));
            map.put("link", task.getLink());
            map.put("priolink", "/w/" + esc(branch) + "/my-tasks/" + esc(task.getId()));
            map.put("parentLink", task.getParentLink());
            map.put("parentTitle", esc(task.getParentTitle()));
            map.put("viewTask", esc(n("viewTask").replace("$t", task.getTypeName())));
            map.put("color", esc(task.getColor()));
            TaskPriority taskPrio = user.getTaskPriority(task.getId());
            map.put("prio", taskPrio.name());
            map.put("prio1", TaskPriority.TOP.equals(taskPrio));
            map.put("prio2", TaskPriority.NORMAL.equals(taskPrio));
            map.put("prio3", TaskPriority.HIDE.equals(taskPrio));
        }
    }
}
