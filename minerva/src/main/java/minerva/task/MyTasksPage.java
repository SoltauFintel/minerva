package minerva.task;

import static minerva.base.StringService.makeClickableLinks;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
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
        boolean showAll = "a".equals(ctx.queryParam("m"));
		
        List<Task> tasks = new TaskService().getTasks(user, branch, login);
        int n = (int) tasks.stream().filter(i -> !TaskPriority.HIDE.equals(user.getTaskPriority(i.getId()))).count();
        if ("master".equals(branch) && me) {
            TaskService.openMasterTasks = n;
            MinervaPageInitializer.updateOpenMasterTasks(this);
        }
        
        header(n("myTasks"));
        putInt("n", n);
        if (tasks.size() != n) {
            putInt("weitere", tasks.size() - n);
            put("hasWeitere", true);
        } else {
            put("hasWeitere", false);
        }
        put("login", esc(UserAccess.login2RealName(StringService.isNullOrEmpty(login) ? user.getLogin() : login)));
		fill(tasks, branch, model, user.getLogin(), showAll);
        put("hasTasks", !tasks.isEmpty());
        put("showTaskButtons", me);
        put("showAll", showAll);
        put("hasHiddenTasks", tasks.stream().anyMatch(i -> TaskPriority.HIDE.equals(user.getTaskPriority(i.getId()))));
        if (showAll) {
            put("showHideLink", "/w/" + esc(branch) + "/my-tasks" + (me ? "" : "?login" + u(login)));
            put("showHideText", esc(n("hideUnimportantTasks")));
        } else {
            put("showHideLink", "/w/" + esc(branch) + "/my-tasks?m=a" + (me ? "" : "&login" + u(login)));
            put("showHideText", esc(n("showAllTasks")));
        }
    }
    
    private void fill(List<Task> tasks, String branch, DataMap model, String login, boolean showAll) {
        DataList list = model.list("tasks");
        fill2(tasks, branch, login, TaskPriority.TOP, list);
        fill2(tasks, branch, login, TaskPriority.NORMAL, list);
        if (showAll) {
            fill2(tasks, branch, login, TaskPriority.HIDE, list);
        }
    }

    private void fill2(List<Task> tasks, String branch, String login, TaskPriority showOnlyPrio, DataList list) {
        for (Task task : tasks) {
            if (!user.getTaskPriority(task.getId()).equals(showOnlyPrio)) {
                continue;
            }
            DataMap map = list.add();
            map.put("me", task.getLogin().equals(login));
            map.put("user", esc(UserAccess.login2RealName(task.getLogin())));
            map.put("date", esc(task.getDateTime()));
            String text = StringService.onlyBody(task.getText());
            map.put("text1", makeClickableLinks(text));
            map.put("hasMoreText", false);
            map.put("completeText", makeClickableLinks(text));
            map.put("link", task.getLink()); // Kommentar anzeigen Link
            map.put("viewTask", esc(getShowNoteLabel(task))); // Kommentar anzeigen Label
            map.put("parentLink", task.getParentLink());
            map.put("parentTitle", esc(task.getParentTitle()));
            map.put("color", esc(task.getColor()));
            TaskPriority taskPrio = user.getTaskPriority(task.getId());
            fillTask(task.getId(), branch, taskPrio, map);
        }
    }
    
    private String getShowNoteLabel(Task task) {
        String typeName = n(task.getTypeName());
        return n("viewTask").replace("$t", typeName);
    }
    
    public static void fillTask(String id, String branch, TaskPriority taskPrio, DataMap map) {
        map.put("id", Escaper.esc(id));
        map.put("priolink", "/w/" + Escaper.esc(branch) + "/my-tasks/" + Escaper.esc(id));
        map.put("prio1", TaskPriority.TOP.equals(taskPrio));
        map.put("prio2", TaskPriority.NORMAL.equals(taskPrio));
        map.put("prio3", TaskPriority.HIDE.equals(taskPrio));
    }
}
