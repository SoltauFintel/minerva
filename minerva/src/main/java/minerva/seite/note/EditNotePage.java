package minerva.seite.note;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import minerva.seite.Note;
import minerva.seite.SPage;

public class EditNotePage extends SPage {

    @Override
    protected void execute() {
        int number = Integer.parseInt(ctx.queryParam("number"));

        Note note = seite.notes().noteByNumber(number);

        if (isPOST()) {
            String text = ctx.formParam("text1");

            note.setText(text);
            note.setChanged(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            seite.saveMeta(seite.getTitle() + ": edit note #" + note.getNumber());
            
            ctx.redirect(viewlink + "/notes");
        } else {
            header(n("editNote"));
            putInt("number", note.getNumber());
            put("user", esc(note.getUser()));
            put("created", esc(note.getCreated()));
            put("changed", esc(note.getChanged()));
            put("hasChanged", !note.getChanged().isEmpty());
            put("text1", esc(note.getText()));
            put("editAllowed", note.getUser().equals(seite.getLogin()));
        }
    }
}
