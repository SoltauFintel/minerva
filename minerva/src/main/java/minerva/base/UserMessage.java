package minerva.base;

import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

/**
 * Message to be displayed to the user, e.g. validation failed.
 */
public class UserMessage extends RuntimeException {

    /**
     * @param key RB key for error message
     * @param workspace for getting user language
     */
    public UserMessage(String key, WorkspaceSO workspace) {
        this(key, workspace.getUser());
    }

    /**
     * @param key RB key for error message
     * @param user for getting user language
     */
    public UserMessage(String key, UserSO user) {
        super(NLS.get(user == null ? "en" : user.getGuiLanguage(), key));
    }
}
