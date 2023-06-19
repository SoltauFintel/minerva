package minerva.seite.note;

import java.util.List;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.seite.Note;

public class AllNotesPage extends BPage {
    
    @Override
    protected void execute() {
        DataList list = list("notes");
        List<NoteWithSeite> allNotes = book.getAllNotes();
        for (NoteWithSeite n : allNotes) {
            DataMap map = list.add();
            Note note = n.getNote();
            map.putInt("number", note.getNumber());
            map.put("user", esc(note.getUser()));
            map.put("created", esc(note.getCreated()));
            String text = note.getText();
            if (text.length() > 113) {
                text = text.substring(0, 110) + "...";
            }
            map.put("text", esc(text));
            String _viewlink = "/s/" + branch + "/" + bookFolder + "/" + n.getSeite().getId();
            map.put("pagelink", _viewlink);
            map.put("link", _viewlink + "/notes");
            map.put("pageTitle", esc(n.getSeite().getTitle()));
            map.put("hasPersons", !note.getPersons().isEmpty());
            map.put("persons", esc(note.getPersons().stream().collect(Collectors.joining(", "))));
        }
        put("bookTitle", esc(book.getTitle()));
        putInt("n", allNotes.size());
        header(n("allNotes"));
    }
}
