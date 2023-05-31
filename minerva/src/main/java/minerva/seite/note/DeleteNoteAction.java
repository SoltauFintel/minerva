package minerva.seite.note;

import minerva.seite.SAction;

public class DeleteNoteAction extends SAction {

    @Override
    protected void execute() {
        int number = Integer.parseInt(ctx.queryParam("number"));

        seite.notes().deleteNote(number);

        ctx.redirect(viewlink + "/notes");
    }
}
