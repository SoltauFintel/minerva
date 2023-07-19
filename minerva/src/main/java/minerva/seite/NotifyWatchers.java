package minerva.seite;

import github.soltaufintel.amalia.mail.Mail;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.model.UserSettingsSO;

public class NotifyWatchers {
    private final MinervaConfig c = MinervaWebapp.factory().getConfig();
    private final SeiteSO seite;
    
    public NotifyWatchers(SeiteSO seite) {
        this.seite = seite;
    }

    public void notifyWatchers() {
        if (!c.readyForWatchNotifications()) {
            return;
        }
        UserSO me = seite.getBook().getWorkspace().getUser();
        for (String login : MinervaWebapp.factory().getLogins()) {
            if (!login.equals(me.getLogin())) {
                UserSettingsSO us = UserSettingsSO.load(login);
                if (us.getWatchlist().contains(seite.getId())) {
                    notifyWatcher(login);
                }
            }
        }
    }
    
    private void notifyWatcher(String login) {
        Mail mail = new Mail();
        mail.setToEmailaddress(c.getMailAddress(login));
        if (StringService.isNullOrEmpty(mail.getToEmailaddress())) {
            return;
        }
        mail.setSubject(c.getWatchSubject());
        mail.setBody(c.getWatchBody()
                .replace("{pageId}", seite.getId())
                .replace("{pageTitle}", Escaper.esc(seite.getTitle()))
                .replace("{bookFolder}", Escaper.esc(seite.getBook().getBook().getFolder()))
                .replace("{branch}", Escaper.esc(seite.getBook().getWorkspace().getBranch())));
        c.sendMail(mail);
    }
}
