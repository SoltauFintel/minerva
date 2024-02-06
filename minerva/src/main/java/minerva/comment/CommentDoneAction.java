package minerva.comment;

import github.soltaufintel.amalia.web.action.Action;
import minerva.base.StringService;

public class CommentDoneAction extends Action {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");
        boolean done = !"u".equals(ctx.queryParam("m"));
        
        CommentService sv = CommentService.service(ctx);
        Comment comment = sv.get(id);
        comment.setDone(done);
        comment.setDoneDate(done ? StringService.now() : "");
        sv.save(comment, done ? "comment done" : "comment undone");
        
        ctx.redirect(sv.getCommentsPagePath());
    }
}
