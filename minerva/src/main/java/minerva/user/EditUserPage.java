package minerva.user;

import org.pmw.tinylog.Logger;

public class EditUserPage extends UPage {

    @Override
    protected void execute() {
        String login = ctx.pathParam("login");

        user.onlyAdmin();
        User u = UserAccess.loadUser(login);

        if (isPOST()) {
            u.setRealName(ctx.formParam("u_name"));
            u.setMailAddress(ctx.formParam("u_mail"));
            u.setExportAllowed("on".equals(ctx.formParam("u_ea")));
            UserAccess.saveUser(u);
            Logger.info(user.getLogin() + " | saved user: " + login);

            ctx.redirect("/users");
        } else {
            header(n("editUser") + ": " + login);
            put("u_login", esc(u.getLogin()));
            put("u_name", esc(u.getRealName()));
            put("u_mail", esc(u.getMailAddress()));
            put("u_ea", u.isExportAllowed());
        }
    }
}
