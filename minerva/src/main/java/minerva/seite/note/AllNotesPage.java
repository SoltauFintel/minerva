package minerva.seite.note;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;

public class AllNotesPage extends BPage {
    // TODO NLS
    
    @Override
    protected void execute() {
        DataList list = list("notes");
        List<NoteWithSeite> allNotes = book.getAllNotes();
        for (NoteWithSeite n : allNotes) {
            DataMap map = list.add();
            map.putInt("number", n.getNote().getNumber());
            map.put("user", esc(n.getNote().getUser()));
            map.put("created", esc(n.getNote().getCreated()));
            String text = n.getNote().getText();
            if (text.length() > 113) {
                text = text.substring(0, 110) + "...";
            }
            map.put("text", esc(text));
            String _viewlink = "/s/" + branch + "/" + bookFolder + "/" + n.getSeite().getId();
            map.put("pagelink", _viewlink);
            map.put("link", _viewlink + "/notes");
            map.put("pageTitle", esc(n.getSeite().getTitle()));
        }
        put("bookTitle", esc(book.getTitle()));
        putInt("n", allNotes.size());
        header("Alle Notizen");
    }
}
