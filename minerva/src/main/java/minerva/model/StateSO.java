package minerva.model;

import minerva.user.User;

public class StateSO {
    private final UserSO user;

    public StateSO(User user) {
        this.user = new UserSO(user);
    }

    public UserSO getUser() {
        return user;
    }
}
