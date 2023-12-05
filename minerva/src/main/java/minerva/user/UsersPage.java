package minerva.user;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

public class UsersPage extends UPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        Logger.info("Users");
        user.log("Users");
        
        header(n("manageUsers"));
        DataList list = list("users");
		UserAccess.loadUsers().stream().sorted((a, b) -> a.getRealName().compareToIgnoreCase(b.getRealName())).forEach(u -> {
		    DataMap map = list.add();
            map.put("login", esc(u.getLogin()));
            map.put("link", esc("/user/" + u.getLogin()));
            map.put("deletelink", esc("/user/" + u.getLogin() + "/delete"));
            map.put("deleteQuestion", esc(n("deleteUserX").replace("$user", u.getLogin())));
            map.put("realName", esc(u.getRealName()));
            map.put("mailAddress", esc(u.getMailAddress()));
            map.put("exportAllowed", u.isExportAllowed());
        });
        putSize("n", list);
    }
}
