package minerva.comment;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.model.UserSO;
import minerva.user.UPage;
import minerva.user.UserAccess;

public class CommentsPage extends Page {

    @Override
    protected void execute() {
        CommentService sv = CommentService.service(ctx);
        List<Comment> comments = sv.getComments();

        DataList allCommentsIdList = list("allCommentsIdList");
        put("book", ctx.pathParam("book"));
        put("branch", ctx.pathParam("branch"));
        put("id", ctx.pathParam("id"));
        put("comments", commentsHTML(comments, sv, 1, allCommentsIdList));
        put("hasComments", !comments.isEmpty());
        put("showTopCreateButton", getOpenCommentsSize(comments) >= 4);

        int openComments = getOpenCommentsSize(comments);
        putInt("openComments", openComments);
        put("hasOpenComments", openComments > 0);
        put("oneOpenComment", openComments == 1);

        sv.initModel(model);
        put("title", NLS.get(sv.getLanguage(), "comments") + " - " + model.get("parentEntityTitle").toString() // after initModel!
                + UPage.TITLE_POSTFIX);
    }

    private String commentsHTML(List<Comment> comments, CommentService sv, int ebene, DataList allCommentsIdList) {
        boolean isAdmin = UserSO.isAdmin(ctx);
        StringBuilder sb = new StringBuilder();
        for (Comment c : comments) {
            DataMap m = new DataMap();
            m.put("commentId", esc(c.getId()));
            allCommentsIdList.add().put("commentId", esc(c.getId()));
            m.put("text", StringService.makeClickableLinks(c.getText()));
            m.put("person", esc(UserAccess.login2RealName(c.getPerson())));
            m.put("hasPerson", !StringService.isNullOrEmpty(c.getPerson()));
            m.put("personIsMe", sv.getLogin().equals(c.getPerson()));
            m.put("changed", c.getChanged());
            m.put("created", c.getCreated());
            m.put("user", esc(UserAccess.login2RealName(c.getUser())));
            m.put("doneDate", c.getDoneDate());
            m.put("hasComments", !c.getComments().isEmpty());
            m.put("comments", commentsHTML(c.getComments(), sv, ebene + 1, allCommentsIdList)); // recursive
            
            m.put("allDone", ebene == 1 && allDone(c));
            m.put("ebene1", ebene == 1);
            m.put("addAllowed", ebene < 7);
            m.put("hasChanged", !c.getChanged().isEmpty());
            m.put("done", c.isDone());
            m.put("editAllowed", isAdmin || (!c.isDone() && (c.getUser() == null || c.getUser().equals(sv.getLogin()))));
            m.put("me", c.getUser() != null && c.getUser().equals(sv.getLogin()));
            m.put("highlight", c.getId().equals(ctx.queryParam("highlight")));
            sv.initModel(c, m);

            sb.append(Page.templates.render("CommentPiece", m));
        }
        return sb.toString();
    }
    
    private boolean allDone(Comment comment) {
        if (comment.isDone()) {
            for (Comment sub : comment.getComments()) {
                if (!allDone(sub)) { // recursive
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private int getOpenCommentsSize(List<Comment> comments) {
        int ret = 0;
        for (Comment comment : comments) {
            if (!comment.isDone()) {
                ret++;
            }
            ret += getOpenCommentsSize(comment.getComments()); // recursive
        }
        return ret;
    }
}
