package minerva.model;

import java.util.Collection;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.spark.Context;
import minerva.auth.MinervaAuth;
import minerva.base.Tosmap;
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
        StateSO state = new StateSO(user);
        state.getUser().finishMyEditings();
        
        UserSettingsSO us = state.getUser().getUserSettings();
        if (us.getGuiLanguage() != null) {
            state.getUser().getUser().setGuiLanguage(us.getGuiLanguage());
        }
        if (us.getPageLanguage() != null) {
            state.getUser().getUser().setPageLanguage(us.getPageLanguage());
        }
        
        int sessionTimeout = 1000 * 60 * 60 * 24 * 7; // I'm not sure what value and if needs to be configurable.
        Tosmap.add(key(ctx), System.currentTimeMillis() + sessionTimeout, state);
        
        // Nach dem Einloggen soll nicht kein Workspace gewählt sein. Daher wird
        // hier der master eingestellt. Zukünftig könnte man sich den zuletzt aktiven
        // Workspace merken.
        try {
            WorkspaceSO workspace = state.getUser().masterWorkspace();
            state.getUser().setCurrentWorkspace(workspace);
        } catch (Exception ignore) {
        }
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
        getStates().stream()
            .filter(state -> !state.getUser().getLogin().equals(login))
            .forEach(state -> state.getUser().onEditing(login, branch, seiteId, finished));
    }
}
