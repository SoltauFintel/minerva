package minerva.comment;

import github.soltaufintel.amalia.web.action.Action;

public class DeleteCommentAction extends Action {

    @Override
    protected void execute() {
        CommentService sv = CommentService.service(ctx);
        sv.delete();
        
        ctx.redirect(sv.getCommentsPagePath());
    }
}
