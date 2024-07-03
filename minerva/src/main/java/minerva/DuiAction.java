package minerva;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Action;
import minerva.validate.ValidatorService.DeleteUnusedImages;

public class DuiAction extends Action {

    @Override
    protected void execute() {
        Logger.info("DuiAction...");
        
        DeleteUnusedImages.start();
        
        Logger.info("DuiAction finished. Going to master menu.");
        ctx.redirect("/w/master/menu");
    }
}
