package minerva.mask.field;

import minerva.mask.MasksService;
import minerva.workspace.WAction;

public class SortMaskFieldAction extends WAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        String id = ctx.pathParam("id");
        boolean up = "up".equals(ctx.queryParam("m"));
        
        new MasksService(workspace).changeOrder(tag, id, up);

        ctx.redirect("/mask/" + branch + "/" + tag);
    }
}
