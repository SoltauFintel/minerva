package minerva.mask;

import org.pmw.tinylog.Logger;

import minerva.workspace.WAction;

public class DeleteMaskAction extends WAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        
        new MasksService(workspace).deleteMask(tag);
        Logger.info(user.getLogin() + " | delete mask " + tag);
        
        ctx.redirect("/mask/" + branch);
    }
}
