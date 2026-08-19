package minerva.book;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.action.Action;
import minerva.MinervaWebapp;
import minerva.base.NlsString;
import minerva.model.StateSO;
import minerva.subscription.SubscriptionService;
import minerva.user.User;

// In einem nackten Minerva wird über diesen Endpoint das erste Buch angelegt.
// Außerdem werden die Seiten dann an die zugehörigen oh-webapps geschickt, damit
// diese die Indizierung durchführen und einen grünen HealthCheck Status bekommen.
public class CreateBookAction extends Action {

    @Override
    protected void execute() {
        String token = MinervaWebapp.factory().getConfig().getAdminToken();
        if (StringService.isNullOrEmpty(token)) {
            ctx.status(401);
            Logger.error("CreateBookAction not accessible because ADMIN_TOKEN is not defined.");
            return;
        }
        String authHeader = ctx.req.headers("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer " + token)) {
            ctx.status(401); // Unauthorized
            Logger.error("CreateBookAction not accessible because authorization failed.");
            return;
        }
        if (MinervaWebapp.factory().isGitlab()) {
            ctx.status(500);
            Logger.error("CreateBookAction is not for Gitlab mode");
            return;
        }
        User user = new User();
        user.setLogin("mwx");
        var workspace = new StateSO(user).getUser().masterWorkspace();
        var oneEmptyBook = workspace.getBooks().size() == 1 && workspace.getBooks().get(0).getSeiten().isEmpty();
        if (!oneEmptyBook && !workspace.getBooks().isEmpty()) {
            ctx.status(500);
            Logger.error("CreateBookAction is not possible because there are already books. You can only create the 1st book.");
            return;
        }
        String folder = ctx.queryParam("folder");
        if (StringService.isNullOrEmpty(folder)) {
            ctx.status(500);
            Logger.error("CreateBookAction: missing parameter 'folder'");
            return;
        }
        String en = ctx.queryParam("en");
        String de = ctx.queryParam("de");
        if (StringService.isNullOrEmpty(en) || StringService.isNullOrEmpty(de)) {
            ctx.status(500);
            Logger.error("CreateBookAction: missing book title parameter 'de' or 'en'");
            return;
        }
        
        if (!oneEmptyBook) {
            NlsString title = new NlsString();
            title.setString("en", en);
            title.setString("de", de);
            workspace.getBooks().createBook(folder, title, MinervaWebapp.factory().getLanguages(), BookType.PUBLIC, 1);
            Logger.info("CreateBookAction successful: " + folder + " | \"" + en + "\", \"" + de + "\"");
        }
        if (workspace.getBooks().size() == 1) {
            workspace.getBooks().get(0).createTopLevelSeite();
        }

        new SubscriptionService().pagesChanged();
    }
}
