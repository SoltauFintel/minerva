package minerva.mask;

import org.pmw.tinylog.Logger;

import gitper.base.StringService;
import minerva.workspace.WPage;

public class AddMaskPage extends WPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String tag = ctx.formParam("tag");
            if (StringService.isNullOrEmpty(tag)) {
                throw new RuntimeException("Bitte tag eingeben!");
            }
            MasksService sv = new MasksService(workspace);
            if (sv.getMasks().stream().anyMatch(i -> i.getTag().equalsIgnoreCase(tag))) {
                throw new RuntimeException("tag bereits vergeben!");
            }
            Mask mask = new Mask();
            mask.setTag(tag);
            sv.saveMask(mask);
            Logger.info(branch + " | " + user.getLogin() + " | Added mask " + tag);

            ctx.redirect("/mask/" + branch + "/" + tag);
        }
    }
}
