package minerva.seite.note;

import minerva.seite.Note;
import minerva.seite.SPage;

public class AddNotePage extends SPage {
    
    @Override
    protected void execute() {
        int parentNumber = Integer.parseInt(ctx.queryParam("parent"));
        if (isPOST()) {
            String text = ctx.formParam("text1");
            if (parentNumber == 0) {
                seite.notes().addNote(text, null);
            } else {
                Note parentNote = seite.notes().noteByNumber(parentNumber);
                seite.notes().addNote(text, parentNote);
            }
            ctx.redirect(viewlink + "/notes");
        } else {
            if (parentNumber == 0) {
                put("parentText", "");
                put("hasParent", false);
            } else {
                put("parentText", esc(seite.notes().noteByNumber(parentNumber).getText()));
                put("hasParent", true);
            }
            header(n("addNote"));
            putInt("parentNumber", parentNumber);
        }
    }
}
