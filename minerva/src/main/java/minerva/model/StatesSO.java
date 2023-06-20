package minerva.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import github.soltaufintel.amalia.spark.Context;
import minerva.user.User;

/**
 * Main entry class for the whole model
 */
public class StatesSO {
    /** key: session ID */
    private static final Map<String, StateSO> states = new HashMap<>();
    
    private StatesSO() {
    }
    
    public static StateSO get(Context ctx) {
        return get(key(ctx));
    }
    
    public static StateSO get(String sessionId) {
        return states.get(sessionId);
    }
    
    public static void login(Context ctx, User user) {
        StateSO state = new StateSO(user);
        states.put(key(ctx), state);
        
        // Nach dem Einloggen soll nicht kein Workspace gewählt sein. Daher wird
        // hier der master eingestellt. Zukünftig könnte man sich den zuletzt aktiven
        // Workspace merken.
        try {
            WorkspaceSO workspace = state.getUser().getWorkspaces().master();
            state.getUser().setCurrentWorkspace(workspace);
        } catch (Exception ignore) {
        }
    }
    
    public static void logout(Context ctx) {
        states.remove(key(ctx));
    }

    private static String key(Context ctx) {
        return ctx.req.session().id();
    }
    
    public static Collection<StateSO> getStates() {
        return states.values();
    }
}
