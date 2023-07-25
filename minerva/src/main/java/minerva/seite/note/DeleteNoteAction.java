package minerva.seite.note;

import minerva.seite.SAction;

public class DeleteNoteAction extends SAction {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");

        seite.notes().deleteNote(id);

        ctx.redirect(viewlink + "/notes");
    }
}
