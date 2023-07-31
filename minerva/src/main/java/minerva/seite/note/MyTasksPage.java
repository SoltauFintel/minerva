package minerva.seite.note;

import org.pmw.tinylog.Logger;

import minerva.base.Uptodatecheck;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
    	String login = ctx.queryParam("login");
		Logger.info(user.getLogin() + " | " + (user.getLogin().equals(login) ? "My tasks" : "All tasks for " + login));
        header(n("myTasks"));
        AllNotesPage.fill(user.getNotes(branch, login), branch, model);
    }
}
