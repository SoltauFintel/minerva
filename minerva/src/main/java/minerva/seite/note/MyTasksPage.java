package minerva.seite.note;

import java.util.List;
import java.util.stream.Collectors;

public class MyTasksPage extends AllNotesPage {

    @Override
    protected List<NoteWithSeite> getNotes() {
        header(n("myTasks"));
        String login = user.getUser().getLogin();
        List<NoteWithSeite> notes = book.getAllNotes();
        return notes.stream()
                .filter(n -> !n.getNote().isDone() && n.getNote().getPersons().contains(login))
                .collect(Collectors.toList());
    }
}
