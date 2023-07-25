package minerva.seite.note;

import minerva.seite.SAction;

public class NoteDoneAction extends SAction {

    @Override
    protected void execute() {
        boolean done = !"u".equals(ctx.queryParam("m"));
        String id = ctx.queryParam("id");

        seite.notes().doneNote(id, done);

        ctx.redirect(viewlink + "/notes");
    }
}
