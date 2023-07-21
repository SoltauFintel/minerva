package minerva.seite.note;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.base.Uptodatecheck;
import minerva.model.BookSO;
import minerva.user.UPage;

public class MyTasksPage extends UPage implements Uptodatecheck {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        Logger.info(user.getUser().getLogin() + " | My tasks");
        header(n("myTasks"));
        AllNotesPage.fill(getNotes(branch), branch, model);
    }

    private List<NoteWithSeite> getNotes(String branch) {
        String login = user.getUser().getLogin();
        List<NoteWithSeite> notes = new ArrayList<>();
        for (BookSO book : user.getWorkspace(branch).getBooks()) {
            for (NoteWithSeite n : book.getSeiten().getAllNotes()) {
                if (!n.getNote().isDone() && n.getNote().getPersons().contains(login)) {
                    notes.add(n);
                }
            }
        }
        return notes;
    }
}
