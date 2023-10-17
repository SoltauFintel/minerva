package minerva.user;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import minerva.MinervaWebapp;
import minerva.base.FileService;
import minerva.base.StringService;

public class UserAccess {

    private UserAccess() {
    }
    
    public static List<User> loadUsers() {
        List<User> ret = new ArrayList<>();
        File[] files = new File(folder()).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".user")) {
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
    
    public static void saveUser(User user) {
        FileService.saveJsonFile(file(user.getLogin()), user);
    }
    
    public static void deleteUser(String login) {
        file(login).delete();
    }

    private static File file(String login) {
        return new File(folder(), login + ".user");
    }

    private static String folder() {
        return MinervaWebapp.factory().getConfig().getWorkspacesFolder();
    }
    
    public static void validateLogin(String login) {
        if (StringService.isNullOrEmpty(login)) {
            throw new RuntimeException("login must not be empty");
        }
        for (int i = 0; i < login.length(); i++) {
            char c = login.charAt(i);
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '.' || c == '-'
                    || c == '_')) {
                throw new RuntimeException("login must contain only these characters: a-z A-Z 0-9 . - _");
            }
        }
    }
    
    public static List<String> getUserNames() {
    	return loadUsers().stream().map(u -> u.getRealName()).collect(Collectors.toList());
    }
    
    public static boolean hasExportRight(String login) {
    	if (MinervaWebapp.factory().getConfig().getAdmins().contains(login)) {
    		return true;
    	}
    	User u = loadUser(login);
    	return u == null || u.isExportAllowed();
    }

    public static String login2RealName(String login) {
    	User u = loadUser(login);
		return u == null || StringService.isNullOrEmpty(u.getRealName()) ? login : u.getRealName();
    }
    
    public static String realName2Login(String realName) {
    	return loadUsers().stream().filter(u -> u.getRealName().equals(realName))
    			.map(u -> u.getLogin()).findFirst().orElse(realName);
    }
}
