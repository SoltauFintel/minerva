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
        this(key, workspace.getUser(), msg -> msg);
    }

    /**
     * @param key RB key for error message
     * @param user for getting user language
     */
    public UserMessage(String key, UserSO user) {
        this(key, user, msg -> msg);
    }

    public UserMessage(String key, WorkspaceSO workspace, ModifyString messageModifier) {
        this(key, workspace.getUser(), messageModifier);
    }

    /**
     * @param key RB key for error message
     * @param user for getting user language
     * @param messageModifier modify msg, used for replacing variables
     */
    public UserMessage(String key, UserSO user, ModifyString messageModifier) {
        super(messageModifier.modify(NLS.get(user == null ? "en" : user.getGuiLanguage(), key)));
    }

    public interface ModifyString {
        String modify(String string);
    }
}
