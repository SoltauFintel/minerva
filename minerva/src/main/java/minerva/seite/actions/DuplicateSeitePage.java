package minerva.seite.actions;

import minerva.seite.SPage;

public class DuplicateSeitePage extends SPage {

    @Override
    protected void execute() {
        String m = ctx.queryParam("m");
        
        if (m != null && m.startsWith("d")) { // "d1" for copying all fields, "d0" for copying some fields
            render = false;
            ctx.redirect("/s-edit/" + branch + "/" + bookFolder + "/" + seite.duplicate(langs, "d1".equals(m)));
        } else { // show question page
            header(n("duplicatePage"));
        }
    }
}
