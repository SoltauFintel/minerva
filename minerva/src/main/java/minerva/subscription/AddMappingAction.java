package minerva.subscription;

import java.util.List;

import minerva.access.CommitMessage;
import minerva.base.StringService;
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
        seite.saveMeta(new CommitMessage(seite, "d".equals(m) ? "remove mapping" : "add mapping"));

        ctx.redirect(viewlink + "/mapping");
    }
}
