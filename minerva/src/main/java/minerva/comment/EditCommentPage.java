package minerva.comment;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Page;
import minerva.base.StringService;
import minerva.user.UserAccess;

/**
 * Edit or add comment
 */
public class EditCommentPage extends Page {

    @Override
    protected void execute() {
        CommentService sv = CommentService.service(ctx);
        String id = ctx.queryParam("id");
        boolean add = StringService.isNullOrEmpty(id);
        String parentId = ctx.queryParam("parent");
        
        Comment c;
        if (add) {
            c = new Comment();
        } else {
            c = sv.get(id);
        }
        
        if (isPOST()) {
            c.setPerson(UserAccess.realName2Login(ctx.formParam("person")));
            c.setText(ctx.formParam("text1")); // TODO strip CRLF at end
            if (add) {
                c.setId(IdGenerator.createId6());
                c.setUser(sv.getLogin());
                c.setCreated(StringService.now());
                c.setParentId(parentId);
                sv.save(c, "comment added");
            } else {
                c.setChanged(StringService.now());
                sv.save(c, "comment modified");
            }
            ctx.redirect(sv.getCommentsPagePath());
        } else {
            sv.initModel(model);
            String title = add ? "Neuen Kommentar eingeben" : "Kommentar bearbeiten";
            put("header", title);
            put("title", title + " - Minerva");
            put("commentId", c.getId());
            put("text1", c.getText());
            combobox("persons", UserAccess.getUserNames(), UserAccess.login2RealName(c.getPerson()), true);
            put("action", ctx.path() + "?" + ctx.req.queryString());
            put("backlink", sv.getCommentsPagePath());
        }
    }
}
