package minerva.seite;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.mail.Mail;
import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.config.MinervaConfig;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.user.User;
import minerva.user.UserAccess;

public class WatchersService {
    private final MinervaConfig c = MinervaWebapp.factory().getConfig();
    private final SeiteSO editedSeite;
    private final UserSO me;
    
    public WatchersService(SeiteSO editedSeite) {
        this.editedSeite = editedSeite;
        me = editedSeite.getBook().getWorkspace().getUser();
    }

    public void notifyWatchers() {
        // Are mail settings complete?
        if (!c.readyForWatchNotifications()) {
            return;
        }
        
        // get all users
        if (me == null) {
        	Logger.error("me is null in WatchersService.notifyWatchers()");
        }
        List<NotifyUser> nu = UserAccess.loadUsers().stream()
                .filter(user -> user != null && user.getLogin() != null && !user.getLogin().equals(me.getLogin()))
                .map(user -> new NotifyUser(user.getMailAddress(), user.getWatchlist()))
                .collect(Collectors.toList());
        Logger.debug("NotifyUser list size: " + nu.size());
        
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
        mail.setSubject(MinervaOptions.MAIL_WATCH_SUBJECT.get()
        		.replace("{pageTitle}", editedSeite.getTitle())); // no esc!
        mail.setBody(c.getWatchBody()  // TODO deprecated, replace to MinervaOptions.MAIL_WATCH_BODY.get()
        		.replace("{url}", getUrl())
                .replace("{pageId}", editedSeite.getId()) // TODO deprecated
                .replace("{pageTitle}", editedSeite.getTitle()) // no esc!
                .replace("{bookFolder}", editedSeite.getBook().getBook().getFolder()) // TODO deprecated
                .replace("{branch}", editedSeite.getBook().getWorkspace().getBranch()) // TODO deprecated
                .replace("{notifiedPage}", user.getNotifiedBecauseOfPage().getTitle()));
        c.sendMail(mail);
    }
    
    private String getUrl() {
    	return "http://" + c.getHost() + "/s/" + editedSeite.getBook().getWorkspace().getBranch() + 
				"/" + editedSeite.getBook().getBook().getFolder() + "/" + editedSeite.getId();
    }
    
    public String getWatchers() {
        Set<String> names = new TreeSet<>();
        List<User> users = UserAccess.loadUsers();
        watch(editedSeite.getId(), users, names);
        SeiteSO parent = editedSeite;
        while (parent.hasParent()) {
            parent = parent.getParent();
            watch(parent.getId() + "+", users, names);
        }
        return names.stream().collect(Collectors.joining(", "));
    }

    private void watch(String x, List<User> users, Set<String> names) {
        for (User user : users) {
            if (user.getWatchlist().contains(x)) {
                names.add(user.getRealName());
            }
        }
    }
}
