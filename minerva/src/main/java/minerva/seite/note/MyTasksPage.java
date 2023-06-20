package minerva.seite.note;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import minerva.model.BookSO;
import minerva.user.UPage;

public class MyTasksPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        header(n("myTasks"));
        put("bookTitle", ""); // TODO
        put("bookFolder", ""); // TODO
        
        String login = user.getUser().getLogin();
        List<NoteWithSeite> notes = new ArrayList<>();
        for (BookSO book : user.getWorkspace(branch).getBooks()) {
            notes.addAll(book.getAllNotes().stream()
                    .filter(n -> !n.getNote().isDone() && n.getNote().getPersons().contains(login))
                    .collect(Collectors.toList()));
        }        
        notes.sort((a, b) -> b.getNote().getCreated().compareTo(a.getNote().getCreated()));
        AllNotesPage.fill(notes, branch, "/s/" + branch + "/$b/", model);
    }
}
