package minerva.comment;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.Page;
import minerva.base.StringService;
import minerva.postcontents.PostContentsService;
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
        id = add ? sv.getParentShortId() + "-add" : id;
        String parentId = ctx.queryParam("parent");

        String key = sv.getKey(id);
        Comment c = add ? new Comment() : sv.get(id);
        
        if (isPOST()) {
            save(sv, add, parentId, key, c);
            ctx.redirect(sv.getCommentsPagePath());
        } else {
            put("add", add);
            put("id", id);
            putInt("version", c.getVersion());
            put("content", c.getText());
            combobox("persons", UserAccess.getUserNames(), UserAccess.login2RealName(c.getPerson()), true);
            put("action", ctx.path() + "?" + ctx.req.queryString());
            put("backlink", sv.getCommentsPagePath());
            put("lang", sv.getLanguage());
            sv.initModel(model);
        }
    }

    private void save(CommentService sv, boolean add, String parentId, String key, Comment c) {
        int version = Integer.parseInt(ctx.formParam("version"));
        CommentPCD data = (CommentPCD) new PostContentsService().waitForContents(key, version);
        
        c.setPerson(UserAccess.realName2Login(data.getPerson()));
        c.setText(data.getText()); // TODO strip CRLF at end
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
    }
}
