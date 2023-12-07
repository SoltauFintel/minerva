package minerva.task;

import org.pmw.tinylog.Logger;

import minerva.workspace.WAction;

public class TaskPrioAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        TaskPriority p = TaskPriority.valueOf(ctx.queryParam("p").toUpperCase());
        
        Logger.info("Task Prio: " + id + ", " + p.name());

        user.setTaskPriority(id, p);
        
        ctx.redirect("/w/" + esc(branch) + "/my-tasks");
    }
}
