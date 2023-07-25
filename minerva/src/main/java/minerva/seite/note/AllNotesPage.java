package minerva.seite.note;

import java.util.List;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.book.BPage;
import minerva.seite.Note;

public class AllNotesPage extends BPage {
    
    @Override
    protected void execute() {
        header(n("allNotes"));
        put("bookTitle", mesc(book.getTitle()));
        fill(book.getSeiten().getAllNotes(), branch, model);
    }
    
    public static void fill(List<NoteWithSeite> notes, String branch, DataMap model) {
        String v0 = "/s/" + branch + "/";
        notes.sort((a, b) -> b.getNote().getCreated().compareTo(a.getNote().getCreated()));
        DataList list = model.list("notes");
        for (NoteWithSeite n : notes) {
            DataMap map = list.add();
            Note note = n.getNote();
            map.put("noteId", note.getId());
            map.put("id", n.getSeite().getId() + "-" + note.getId());
            map.put("user", mesc(note.getUser()));
            map.put("created", mesc(note.getCreated()));
            String text = note.getText();
            if (text.length() > 113) {
                map.put("text1", mesc(text.substring(0, 110)));
                map.put("hasMoreText", true);
            } else {
                map.put("text1", mesc(text));
                map.put("hasMoreText", false);
            }
            map.put("completeText", mesc(text));
            String bookFolder = n.getSeite().getBook().getBook().getFolder();
            String v = v0 + bookFolder + "/" + n.getSeite().getId();
            map.put("pagelink", v);
            map.put("link", v + "/notes");
            map.put("bookTitle", mesc(n.getSeite().getBook().getTitle()));
            map.put("pageTitle", mesc(n.getSeite().getTitle()));
            map.put("hasPersons", !note.getPersons().isEmpty());
            map.put("persons", mesc(note.getPersons().stream().collect(Collectors.joining(", "))));
        }
        model.putInt("n", notes.size());
    }
    
    private static String mesc(String text) {
        return Escaper.esc(text);
    }
}
