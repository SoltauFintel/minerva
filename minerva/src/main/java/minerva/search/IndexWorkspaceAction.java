package minerva.search;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.user.UAction;

public class IndexWorkspaceAction extends UAction {
    private static long lastCall = 0;
    
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");

        if (MinervaWebapp.factory().isCustomerVersion()) {
            long now = System.currentTimeMillis();
            long diff = now - lastCall;
            int limit = 60 * 1000; // TODO Wert 60 param.
            Logger.info("IndexWorkspaceAction lastCall: " + lastCall + ", now: " + now + ", diff: " + diff + ", limit: " + limit);
            if (limit > 0 && lastCall != 0 && diff <= limit) {
                throw new RuntimeException("Reindexing can not be performed that often. Please try again later.");
            }
        } else {
            user.onlyAdmin();
        }
        
        user.log(branch + " | Indexing...");
        user.getWorkspace(branch).getSearch().indexBooks();
        user.log(branch + " | Indexing finished.");
        lastCall = System.currentTimeMillis();

        ctx.redirect("/message?m=3");
    }
}
