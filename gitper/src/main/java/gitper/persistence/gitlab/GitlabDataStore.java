package gitper.persistence.gitlab;

import java.util.HashMap;
import java.util.Map;

import gitper.User;

public class GitlabDataStore {
    private static final Map<String, Map<String, String>> dataStore = new HashMap<>();
    private final String login;

    public GitlabDataStore(User user) {
        login = user.getLogin();
    }

    public String getPassword() {
        return get(login, "password");
    }

    public void setPassword(String password) {
        put(login, "password", password);
    }

    public String getAccessToken() {
        return get(login, "accessToken");
    }

    public void setAccessToken(String accessToken) {
        put(login, "accessToken", accessToken);
    }

    public String getRefreshToken() {
        return get(login, "refreshToken");
    }

    public void setRefreshToken(String refreshToken) {
        put(login, "refreshToken", refreshToken);
    }

    public static String get(String login, String fieldname) {
        Map<String, String> map = dataStore.get(login);
        return map == null ? null : map.get(fieldname);
    }

    public static void put(String login, String fieldname, String value) {
        Map<String, String> map = dataStore.get(login);
        if (map == null) {
            map = new HashMap<>();
            dataStore.put(login, map);
        }
        map.put(fieldname, value);
    }

    public static void clear(String login) {
        dataStore.remove(login);
    }
}
