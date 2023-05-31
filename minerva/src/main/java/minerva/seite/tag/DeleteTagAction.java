package minerva.seite.tag;

import minerva.seite.SAction;

public class DeleteTagAction extends SAction {

    @Override
    protected void execute() {
        String tag = ctx.queryParam("tag");

        seite.tags().deleteTag(tag);
        
        ctx.redirect(viewlink + "/tags");
    }
}
