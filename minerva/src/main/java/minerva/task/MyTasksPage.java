package minerva.task;

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
		Logger.info(user.getLogin() + " | " + (login == null || user.getLogin().equals(login) ? "My tasks" : "All tasks for " + login));
        
        List<Task> tasks = new TaskService().getTasks(user, branch, login);
        if ("master".equals(branch)) {
            TaskService.openMasterTasks = tasks.size();
            MinervaPageInitializer.updateOpenMasterTasks(this);
        }
        
        header(n("myTasks"));
        put("login", esc(UserAccess.login2RealName(StringService.isNullOrEmpty(login) ? user.getLogin() : login)));
		fill(tasks, branch, model, user.getLogin());
        putSize("n", tasks);
        put("hasTasks", !tasks.isEmpty());
    }
    
    private void fill(List<Task> tasks, String branch, DataMap model, String login) {
        DataList list = model.list("tasks");
        for (Task task : tasks) {
            DataMap map = list.add();
            map.put("id", task.getId());
            map.put("me", task.getLogin().equals(login));
            map.put("user", esc(UserAccess.login2RealName(task.getLogin())));
            map.put("date", esc(task.getDateTime()));
            String text = task.getText();
            if (text.length() > 113) {
                map.put("text1", StringService.makeClickableLinks(esc(text.substring(0, 110))));
                map.put("hasMoreText", true);
            } else {
                map.put("text1", StringService.makeClickableLinks(esc(text)));
                map.put("hasMoreText", false);
            }
            map.put("completeText", StringService.makeClickableLinks(esc(text)));
            map.put("link", task.getLink());
            map.put("parentLink", task.getParentLink());
            map.put("parentTitle", esc(task.getParentTitle()));
            map.put("viewTask", esc(n("viewTask").replace("$t", task.getTypeName())));
            map.put("color", esc(task.getColor()));
        }
    }
}
