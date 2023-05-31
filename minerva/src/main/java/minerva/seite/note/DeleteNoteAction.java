package minerva.seite.note;

import minerva.seite.SAction;

public class DeleteNoteAction extends SAction {

    @Override
    protected void execute() {
        int number = Integer.parseInt(ctx.queryParam("number"));

        seite.deleteNote(number);

        ctx.redirect(viewlink + "/notes");
    }
}
