package minerva.persistence.gitlab;

import minerva.MinervaWebapp;
import minerva.auth.LoginService;
import minerva.base.StringService;
import minerva.user.User;

public class GitlabLoginService implements LoginService {

    @Override
    public boolean withPassword() {
        return true;
    }

    @Override
    public User login(String login, String password) {
        if (StringService.isNullOrEmpty(login) || StringService.isNullOrEmpty(password)) {
            return null;
        }
        GitlabUser user = new GitlabUser(login, password);
        String mail = MinervaWebapp.factory().getGitlabSystem().login(user);
        if (mail == null) {
            return null;
        }
        user.setMail(mail);
        return user;
    }
}
