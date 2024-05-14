package minerva.seite;

import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.mail.Mail;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.user.UserAccess;

public class NotifyWatchers {
    private final MinervaConfig c = MinervaWebapp.factory().getConfig();
    private final SeiteSO editedSeite;
    private final UserSO me;
    
    public NotifyWatchers(SeiteSO editedSeite) {
        this.editedSeite = editedSeite;
        me = editedSeite.getBook().getWorkspace().getUser();
    }

    public void notifyWatchers() {
        // Are mail settings complete?
        if (!c.readyForWatchNotifications()) {
            return;
        }
        
        // get all users
        List<NotifyUser> nu = UserAccess.loadUsers().stream()
                .filter(user -> !user.getLogin().equals(me.getLogin()))
                .map(user -> new NotifyUser(user.getMailAddress(), user.getWatchlist()))
                .collect(Collectors.toList());
        
        // collect all watchers
        findWatchers(editedSeite, nu, false);

        // notify all watchers
        nu.stream().filter(i -> i.isNotify()).forEach(user -> notifyWatcher(user));
    }
    
    private static class NotifyUser {
        private final String mailAddress;
        private final List<String> watchlist;
        private boolean notify = false;
        private SeiteSO notifiedBecauseOfPage;

        private NotifyUser(String mailAddress, List<String> watchlist) {
            this.mailAddress = mailAddress;
            this.watchlist = watchlist;
        }
        
        String getMailAddress() {
            return mailAddress;
        }

        List<String> getWatchlist() {
            return watchlist;
        }

        boolean isNotify() {
            return notify;
        }

        void setNotify(boolean notify) {
            this.notify = notify;
        }

        SeiteSO getNotifiedBecauseOfPage() {
            return notifiedBecauseOfPage;
        }

        void setNotifiedBecauseOfPage(SeiteSO notifiedBecauseOfPage) {
            this.notifiedBecauseOfPage = notifiedBecauseOfPage;
        }
    }
    
    private void findWatchers(SeiteSO seite, List<NotifyUser> nu, boolean subpages) {
        for (NotifyUser user : nu) {
            if (!user.isNotify() && user.getWatchlist().contains(seite.getId() + (subpages ? "+" : ""))) {
                user.setNotify(true);
                user.setNotifiedBecauseOfPage(seite);
            }
        }
        if (seite.hasParent()) {
            findWatchers(seite.getParent(), nu, true); // recursive
        }
    }
    
    private void notifyWatcher(NotifyUser user) {
        if (StringService.isNullOrEmpty(user.getMailAddress())) {
            return;
        }
        Mail mail = new Mail();
        mail.setToEmailaddress(user.getMailAddress());
        mail.setSubject(c.getWatchSubject());
        mail.setBody(c.getWatchBody()
                .replace("{pageId}", editedSeite.getId())
                .replace("{pageTitle}", editedSeite.getTitle()) // no esc!
                .replace("{bookFolder}", editedSeite.getBook().getBook().getFolder())
                .replace("{branch}", editedSeite.getBook().getWorkspace().getBranch())
                .replace("{notifiedPage}", user.getNotifiedBecauseOfPage().getTitle()));
        c.sendMail(mail);
    }
}
