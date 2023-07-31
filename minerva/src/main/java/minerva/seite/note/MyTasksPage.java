package minerva.seite.note;

import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
    	String login = ctx.queryParam("login");
		Logger.info(user.getLogin() + " | " + (user.getLogin().equals(login) ? "My tasks" : "All tasks for " + login));
		put("login", esc(StringService.isNullOrEmpty(login) ? user.getLogin() : login));
        header(n("myTasks"));
        List<NoteWithSeite> notes = user.getNotes(branch, login);
		AllNotesPage.fill(notes, branch, model);
		putInt("n", notes.size());
    }
}
