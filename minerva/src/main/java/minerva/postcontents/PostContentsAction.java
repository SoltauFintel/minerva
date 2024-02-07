package minerva.postcontents;

import github.soltaufintel.amalia.web.action.Action;

public class PostContentsAction extends Action {
    
    @Override
    protected void execute() {
        new PostContentsService().processContent(ctx);
    }
}
