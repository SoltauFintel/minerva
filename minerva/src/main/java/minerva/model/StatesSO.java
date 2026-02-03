package minerva.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.auth.MinervaAuth;
import minerva.base.MinervaMetrics;
import minerva.base.Tosmap;
import minerva.base.UserMessage;
import minerva.config.MinervaOptions;
import minerva.user.User;

/**
 * Main entry class for the whole model
 */
public class StatesSO {
    
    private StatesSO() {
    }
    
    public static StateSO get(Context ctx) {
        StateSO state = get(key(ctx));
        if (state == null) {
            MinervaAuth.logout2(ctx);
            throw new SessionExpiredException();
        }
        return state;
    }
    
    public static StateSO get(String sessionId) {
        return (StateSO) Tosmap.get(sessionId);
    }
    
    public static void login(Context ctx, User user) {
        try {
            StateSO state = new StateSO(user);
            state.getUser().finishMyEditings();
            
            int sessionTimeout = 1000 * 60 * 60 * 12; // I'm not sure what value and if needs to be configurable.
            Tosmap.add(key(ctx), sessionTimeout, state);
            
            // Nach dem Einloggen soll nicht kein Workspace gewählt sein. Daher wird
            // hier der master eingestellt. Zukünftig könnte man sich den zuletzt aktiven
            // Workspace merken.
            try {
                WorkspaceSO workspace = state.getUser().masterWorkspace();
                state.getUser().setCurrentWorkspace(workspace);
            } catch (Exception ignore) {
            }
            MinervaMetrics.LOGIN.inc();
        } catch (Exception e) {
            Logger.error(e, "Error logging in"); // Important so that errors are not missed when logging in.
        }
    }
    
    /**
     * Login for cron timers
     * @return UserSO
     */
    public static UserSO login() {
        if (!MinervaOptions.CLEANUP_LOGIN.isSet() || !MinervaOptions.CLEANUP_PASSWORD.isSet()) {
            Logger.error("Cleanup login and/or password are not set in configuration. Go to Menu (in admin mode) > Configuration to enter the needed values.");
            return null;
        }
        gitper.User user = MinervaWebapp.factory().getBackendService()
                .login(MinervaOptions.CLEANUP_LOGIN.get(), MinervaOptions.CLEANUP_PASSWORD.get(), null);
        MinervaMetrics.LOGIN_AUTOMATIC.inc();
        return user == null ? null : new UserSO((User) user);
    }

    public static void logout(Context ctx) {
        Tosmap.remove(key(ctx));
    }

    private static String key(Context ctx) {
        return ctx.req.session().id();
    }
    
    public static Collection<StateSO> getStates() {
        return Tosmap.getValues().stream()
                .filter(i -> i instanceof StateSO)
                .map(i -> (StateSO) i)
                .collect(Collectors.toList());
    }

    public static void onPush(String login, String branch) {
        // Inform all other active users about the push.
        getStates().stream()
            .filter(state -> !state.getUser().getLogin().equals(login))
            .forEach(state -> state.getUser().addHasToPull(branch));
    }
    
    public static void onEditing(String login, String branch, String seiteId, boolean finished) {
        // Inform all other active users about the start/end of editing the page.
        getStates().stream().forEach(state -> state.getUser().onEditing(login, branch, seiteId, finished));
    }

    public static class SessionExpiredException extends UserMessage {

        public SessionExpiredException() {
            super("session-expired", (UserSO) null);
        }
    }
    
    public static List<WorkspaceSO> getWorkspacesForTimer(String branches) {
        List<WorkspaceSO> ret = new ArrayList<>();
        if (branches != null) {
            UserSO user = login();
            for (String branch : branches.split(",")) {
                try {
                    ret.add(user.getWorkspace(branch.trim()));
                } catch (Exception e) {
                    Logger.error("StatesSO.getWorkspacesForTimer() cannot access branch " + branch + ": "
                            + e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }
        return ret;
    }
    
    public static void updatePagesMetrics() {
        Logger.info("updatePagesMetrics");
        try {
            long users = 0;
            long workspaces = 0;
            long books = 0;
            long pages = 0;
            for (Object o : Tosmap.getValues()) {
                if (o instanceof StateSO state) {
                    users++;
                    var userWorkspaces = state.getUser().getWorkspaces();
                    workspaces += userWorkspaces.size();
                    for (WorkspaceSO workspace : userWorkspaces) {
                        for (BookSO book : workspace.getBooks()) {
                            pages += book.getSeiten().countAll();
                        }
                        books += workspace.getBooks().size();
                    }
                }
            }
            MinervaMetrics.USERS_IN_MEMORY.set(users);
            MinervaMetrics.WORKSPACES_IN_MEMORY.set(workspaces);
            MinervaMetrics.BOOKS_IN_MEMORY.set(books);
            MinervaMetrics.PAGES_IN_MEMORY.set(pages);
        } catch (Exception e) {
            Logger.error(e);
        }
    }
}
