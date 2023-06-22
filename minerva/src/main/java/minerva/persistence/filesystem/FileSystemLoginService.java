package minerva.persistence.filesystem;

import org.pmw.tinylog.Logger;

import minerva.auth.LoginService;
import minerva.base.StringService;
import minerva.user.User;

public class FileSystemLoginService implements LoginService {
    // TO-DO Es wird später noch eine Variante für F1 geben, bei dem via POST Request das Einloggen erfolgt.

    @Override
    public boolean withPassword() {
        return false;
    }

    @Override
    public User login(String login, String password) {
        if (StringService.isNullOrEmpty(login)) {
            return null;
        }
        String folder = System.getenv("MINERVA_USERFOLDER");
        if (StringService.isNullOrEmpty(folder)) {
            folder = login;
        }
        Logger.debug(login + " | folder: " + folder);
        return new User(login, folder);
    }
}
