package minerva.mask.field;

import minerva.mask.Mask;
import minerva.mask.MasksService;
import minerva.workspace.WAction;

public class DeleteMaskFieldAction extends WAction {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        String id = ctx.pathParam("id");

        MasksService sv = new MasksService(workspace);
        Mask mask = sv.getMask(tag);
        if (mask != null) {
            int n = mask.getFields().size();
            mask.getFields().removeIf(i -> i.getId().equals(id));
            if (mask.getFields().size() < n) {
                sv.saveMask(mask);
            }
        }

        ctx.redirect("/mask/" + branch + "/" + tag);
    }
}
