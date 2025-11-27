package minerva.user.quickbuttons;

import minerva.user.UPage;

public class EditQuickbuttonPage extends UPage {

    @Override
    protected void execute() {
        var id = ctx.queryParam("id");

        var qb = user.getUser().getQuickbutton(id);
        if (isPOST()) {
            String label = ctx.formParam("label");
            String link = ctx.formParam("link");
            
            qb.setLabel(label);
            qb.setLink(link);
            user.saveQuickbuttons();
            
            ctx.redirect("/q/config");
        } else {
            header(n("editQuickbutton"));
            put("label", esc(qb.getLabel()));
            put("link", esc(qb.getLink()));
        }
    }
}
