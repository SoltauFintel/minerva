package minerva.seite.tag;

import minerva.seite.SAction;

public class DeleteTagAction extends SAction {

    @Override
    protected void execute() {
        String tag = ctx.queryParam("tag");

        seite.removeTag(tag);
        
        ctx.redirect(viewlink + "/tags");
    }
}
