package minerva.user;

import org.pmw.tinylog.Logger;

import gitper.base.StringService;
import minerva.MinervaWebapp;

public class DeleteUserAction extends UAction {

    @Override
    protected void execute() {
        String login = ctx.pathParam("login");
        if (StringService.isNullOrEmpty(login) || login.contains("/") || login.contains("\\") || login.contains(":")) {
            user.log("delete user error -> illegal login: " + login);
            throw new RuntimeException("Illegal parameter 'login'!");
        } else if (login.equalsIgnoreCase(user.getLogin())) {
            user.log("delete user '" + login + "' error -> self");
            throw new RuntimeException("You can't delete yourself!");
        } else if (MinervaWebapp.factory().getAdmins().contains(login)) {
            user.log("delete user '" + login + "' error -> admin");
            throw new RuntimeException("Admin can't be deleted!");
        }
        
        UserAccess.deleteUser(login);
        user.log("User deleted: " + login);
        Logger.info(user.getLogin() + " | User deleted: " + login);
        
        ctx.redirect("/users");
    }
}
