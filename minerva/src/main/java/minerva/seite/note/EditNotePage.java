package minerva.seite.note;

import java.util.List;

import minerva.MinervaWebapp;
import minerva.seite.Note;
import minerva.seite.SPage;

public class EditNotePage extends SPage {

    @Override
    protected void execute() {
        if (ctx.queryParam("id") == null) {
            throw new RuntimeException("Can't display page because parameter id isn't set.");
        }
        String id = ctx.queryParam("id");

        Note note = seite.notes().find(id);
        if (note == null) {
            throw new RuntimeException("Note not found");
        }

        if (isPOST()) {
            String text = ctx.formParam("text1");
            List<String> persons = AddNotePage.toPersons(ctx);
            seite.notes().saveEditedNote(text, persons, note);
            ctx.redirect(viewlink + "/notes");
        } else {
            header(n("editNote"));
            put("noteId", note.getId());
            put("user", esc(note.getUser()));
            put("created", esc(note.getCreated()));
            put("changed", esc(note.getChanged()));
            put("hasChanged", !note.getChanged().isEmpty());
            put("text1", esc(note.getText()));
            put("editAllowed", note.getUser().equals(seite.getLogin()) || isAdmin);
            combobox("persons", MinervaWebapp.factory().getPersons(), note.getPersons(), true, model);
        }
    }
}
