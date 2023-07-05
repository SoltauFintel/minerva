package minerva.persistence.filesystem;

import org.pmw.tinylog.Logger;

import minerva.auth.Book6LoginAction;
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
        if (!Book6LoginAction.PASSWORD.equals(password)) {
            return null;
        }
        // ~duplicate code>>
        String folder = System.getenv("MINERVA_USERFOLDER");
        if (StringService.isNullOrEmpty(folder)) {
            folder = login;
        }
        Logger.debug(login + " | folder: " + folder);
        return new User(login, folder);
        // <<
    }
}
