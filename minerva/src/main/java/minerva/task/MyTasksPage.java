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
import minerva.model.UserSO;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {
    
    @Override
    protected void execute() {
        String login = ctx.queryParam("login");
        boolean showAll = "a".equals(ctx.queryParam("m"));

        boolean me = false;
        List<Task> tasklist;
        if (StringService.isNullOrEmpty(login) || user.getLogin().equals(login)) {
            me = true;
            tasklist = init(user, me);
            Logger.info(user.getLogin() + " | My tasks");
            header(n("myTasks"));
        } else {
            tasklist = init(new UserSO(UserAccess.loadUser(login)), me);
            Logger.info(user.getLogin() + " | All tasks for " + login);
            header(n("Tasks"));
        }        
        
        fill(tasklist, branch, model, user.getLogin(), showAll);
        put("hasTasks", !tasklist.isEmpty());
        put("showTaskButtons", me);
        put("showAll", showAll);
        put("hasHiddenTasks", tasklist.stream().anyMatch(i -> TaskPriority.HIDE.equals(user.getTaskPriority(i.getId()))));
        if (showAll) {
            put("showHideLink", "/w/" + esc(branch) + "/my-tasks" + (me ? "" : "?login" + u(login)));
            put("showHideText", esc(n("hideUnimportantTasks")));
        } else {
            put("showHideLink", "/w/" + esc(branch) + "/my-tasks?m=a" + (me ? "" : "&login" + u(login)));
            put("showHideText", esc(n("showAllTasks")));
        }
    }
    
    private List<Task> init(UserSO pUser, boolean me) {
        List<Task> tasks = new TaskService().getTasks(user/*current user for loading data!*/, branch, pUser.getLogin());
        
        int size = tasks.size();
        int n = (int) tasks.stream().filter(i -> !TaskPriority.HIDE.equals(pUser.getTaskPriority(i.getId()))).count();
        if ("master".equals(branch)) {
            TaskService.update(pUser, n);
            if (me) {
                MinervaPageInitializer.fillNumberOfOpenMasterTasks(n, this);
            }
        }

        putInt("n", n);
        put("hasWeitere", size != n);
        putInt("weitere", size - n);
        put("login", Escaper.esc(UserAccess.login2RealName(pUser.getLogin())));
        return tasks;
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
            map.put("viewTask", esc(getShowCommentLabel(task))); // Kommentar anzeigen Label
            map.put("parentLink", task.getParentLink());
            map.put("parentTitle", esc(task.getParentTitle()));
            map.put("color", esc(task.getColor()));
            TaskPriority taskPrio = user.getTaskPriority(task.getId());
            fillTask(task.getId(), branch, taskPrio, map);
        }
    }
    
    private String getShowCommentLabel(Task task) {
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
