package minerva.subscription;

import github.soltaufintel.amalia.web.action.Action;

/**
 * OH (Abonnent) meldet sich bei Minerva an.
 * Minerva prüft dann, ob der Abonnent gültig ist und pusht dann alle Daten an diesen Abonnenten.
 */
public class SubscribeAction extends Action {

    @Override
    protected void execute() {
        String url = ctx.queryParam("url");
        new SubscriptionService().subscribe(url);
    }
}
