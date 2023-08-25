package minerva.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.base.FileService;

public class UserAccess {

    private UserAccess() {
    }
    
    public static List<User> loadUsers() {
        List<User> ret = new ArrayList<>();
        File[] files = new File(folder()).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("user-") && file.getName().endsWith(".json")) {
                    ret.add(FileService.loadJsonFile(file, User.class));
                }
            }
        }
        return ret;
    }
    
    public static User loadUser(String login) {
        File file = file(login);
        if (file.isFile()) {
            return FileService.loadJsonFile(file, User.class);
        }
        return null;
    }
    
    public static void save(User user) {
        FileService.saveJsonFile(file(user.getLogin()), user);
    }
    
    public static void delete(String login) {
        file(login).delete();
    }

    private static File file(String login) {
        return new File(folder(), "user-" + login + ".json");
    }

    private static String folder() {
        return MinervaWebapp.factory().getConfig().getWorkspacesFolder();
    }
}
