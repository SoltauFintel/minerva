package minerva.task;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.workspace.WAction;

public class TaskPrioAction extends WAction {
    private String html;
    
    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        TaskPriority taskPrio = TaskPriority.valueOf(ctx.queryParam("p").toUpperCase());
        
        Logger.debug("task prio for " + id + ": " + taskPrio.name());

        user.setTaskPriority(id, taskPrio);
        
        DataMap model = new DataMap();
        MyTasksPage.fillTask(id, branch, taskPrio, model.createObject("n"));
        html = Page.templates.render("taskbuttons2", model);
    }
    
    @Override
    protected String render() {
        return html;
    }
}
