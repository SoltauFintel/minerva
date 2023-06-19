package minerva.seite.note;

import minerva.seite.SAction;

public class NoteDoneAction extends SAction {

    @Override
    protected void execute() {
        boolean done = !"u".equals(ctx.queryParam("m"));
        int number = Integer.parseInt(ctx.queryParam("number"));

        seite.notes().doneNote(number, done);

        ctx.redirect(viewlink + "/notes");
    }
}
