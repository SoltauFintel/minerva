package minerva.user;

import java.util.ArrayList;
import java.util.List;

public class UserSettings {
    private final List<String> favorites = new ArrayList<>();

    public List<String> getFavorites() {
        return favorites;
    }
}
