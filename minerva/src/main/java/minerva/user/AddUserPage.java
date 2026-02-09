package minerva.user;

import org.pmw.tinylog.Logger;

import gitper.base.StringService;

public class AddUserPage extends UPage {

    @Override
    protected void execute() {
        onlyAdmin();

        if (isPOST()) {
            User u = new User();
            u.setLogin(ctx.formParam("u_login"));
            u.setRealName(ctx.formParam("u_name"));
            u.setMailAddress(ctx.formParam("u_mail"));
            u.setExportAllowed("on".equals(ctx.formParam("u_ea")));
            u.setInitialien(initialienAutomatik(u.getRealName()));

            if (StringService.isNullOrEmpty(u.getLogin())) {
                throw new RuntimeException("Bitte Login eingeben!");
            } else if (StringService.isNullOrEmpty(u.getRealName())) {
                throw new RuntimeException("Bitte Namen eingeben!");
            } else if (StringService.isNullOrEmpty(u.getMailAddress())) {
                throw new RuntimeException("Bitte Emailadresse eingeben!");
            } else if (UserAccess.loadUser(u.getLogin()) != null) {
                throw new RuntimeException("Login '" + u.getLogin() + "' bereits vergeben!");
            }
            
            UserAccess.saveUser(u);
            Logger.info(user.getLogin() + " | saved new user: " + u.getLogin());

            ctx.redirect("/users");
        } else {
            header(n("addUser"));
        }
    }

    private String initialienAutomatik(String name) {
        if (name != null && !name.isBlank()) {
            name = name.trim();
            int o = name.lastIndexOf(" ");
            try {
                return ("" + name.charAt(0) + name.charAt(o + 1)).toUpperCase();
            } catch (Exception e) {
            }
        }
        return "";
    }
}
