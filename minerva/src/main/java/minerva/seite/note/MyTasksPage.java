package minerva.seite.note;

import org.pmw.tinylog.Logger;

import minerva.base.Uptodatecheck;
import minerva.workspace.WPage;

public class MyTasksPage extends WPage implements Uptodatecheck {

    @Override
    protected void execute() {
        Logger.info(user.getLogin() + " | My tasks");
        header(n("myTasks"));
        AllNotesPage.fill(user.getNotes(branch), branch, model);
    }
}
