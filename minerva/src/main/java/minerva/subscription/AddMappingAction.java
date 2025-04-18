package minerva.subscription;

import java.util.List;

import gitper.base.StringService;
import minerva.seite.SAction;

public class AddMappingAction extends SAction {

    @Override
    protected void execute() {
        String ohId = ctx.queryParam("ohid");
        String m = ctx.queryParam("m"); // d: delete, f: add with force mode, else: add normal
        if (StringService.isNullOrEmpty(ohId)) {
            throw new RuntimeException("Parameter ohid must not be empty!");
        }
        
        List<String> hk = seite.getSeite().getHelpKeys();
        hk.remove(ohId);
        hk.remove(ohId + "!");
        if ("f".equals(m)) {
            hk.add(ohId + "!");
        } else if (!"d".equals(m)) {
            hk.add(ohId);
        }
        seite.saveMeta(seite.commitMessage("d".equals(m) ? "remove mapping" : "add mapping"));
        
        seite.updateOnlineHelp();

        ctx.redirect(viewlink + "/mapping");
    }
}
