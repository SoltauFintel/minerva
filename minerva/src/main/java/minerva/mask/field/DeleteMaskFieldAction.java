package minerva.mask.field;

import minerva.mask.MasksService;
import minerva.workspace.WAction;

public class DeleteMaskFieldAction extends WAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        String id = ctx.pathParam("id");

        new MasksService(workspace).deleteMaskField(tag, id);

        ctx.redirect("/mask/" + esc(branch) + "/" + esc(tag));
    }
}
