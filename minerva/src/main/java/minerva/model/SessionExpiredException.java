package minerva.model;

import minerva.base.UserMessage;

public class SessionExpiredException extends UserMessage {

    public SessionExpiredException() {
        super("session-expired", (UserSO) null);
    }
}
