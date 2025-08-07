package minerva.user;

import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

/**
 * Manage users
 */
public class UsersPage extends UPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        Logger.info("Manage users");
        user.log("Manage users");
        
        Stream<User> users = UserAccess.loadUsers().stream().sorted((a, b) -> a.getRealName().compareToIgnoreCase(b.getRealName()));

        header(n("manageUsers"));
        DataList list = list("users");
        users.forEach(u -> {
            DataMap map = list.add();
            map.put("login", esc(u.getLogin()));
            map.put("link", esc("/user/" + u.getLogin()));
            map.put("deletelink", esc("/user/" + u.getLogin() + "/delete"));
            map.put("deleteQuestion", esc(n("deleteUserX").replace("$user", u.getLogin())));
            map.put("realName", esc(u.getRealName()));
            map.put("mailAddress", esc(u.getMailAddress()));
            map.put("exportAllowed", u.isExportAllowed());
            map.put("init", esc(u.getInitialien()));
            map.put("cr", esc(u.getCustomerRights()));
        });
        putSize("n", list);
    }
}
