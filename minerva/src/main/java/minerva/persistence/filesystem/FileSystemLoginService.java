package minerva.persistence.filesystem;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.auth.LoginService;
import minerva.base.StringService;
import minerva.user.User;

public class FileSystemLoginService implements LoginService {

    @Override
    public boolean withPassword() {
        return false;
    }

    @Override
    public User login(String login, String password) {
        if (StringService.isNullOrEmpty(login)) {
            return null;
        }
        if (!MinervaWebapp.factory().getConfig().getEditorPassword().equals(password)) {
            return null;
        }
        return loginUser(login);
    }
    
    public static User loginUser(String login) {
        String folder = MinervaWebapp.factory().getConfig().getUserFolder();
        if (folder.isEmpty()) {
            folder = login;
        }
        Logger.debug(login + " | folder: " + folder);
        return new User(login, folder);
    }
}
