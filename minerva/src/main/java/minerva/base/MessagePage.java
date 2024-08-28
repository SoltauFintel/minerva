package minerva.base;

import org.pmw.tinylog.Logger;

import minerva.user.UPage;

public class MessagePage extends UPage {

    @Override
    protected void execute() {
        int m = Integer.parseInt(ctx.queryParam("m"));
        
        header(n("message"));
        put("p", "");
        put("link", "/");
        put("linkTitle", n("goToHomepage"));

        switch (m) {
        case 1: // ViewSeitePage
            put("h2", n("pageNotFound"));
            put("p", n("pageNotFound2"));
            break;
        case 2: // AddWorkspacePage
            put("h2", n("noBranchToAdd"));
            break;
        case 3: // IndexWorkspaceAction
            put("h2", n("reindex-complete"));
            break;
        case 4: // Book6StartAction
            put("h2", "No access granted.");
            break;
        case 5: // XMinerva UpdateFeatureNumbersInJiraAction
        	put("h2", n("featureNumbersToJira"));
        	break;
        default:
            Logger.error("[MessagePage] unsupported m value: " + m);
            put("h2", "Sorry, no message text for this message.");
        }
    }
}
