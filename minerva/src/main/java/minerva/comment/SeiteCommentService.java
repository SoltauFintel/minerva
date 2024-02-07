package minerva.comment;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.access.SimpleDirAccess;
import minerva.base.NLS;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

/**
 * Seite-specific comments
 */
public class SeiteCommentService extends CommentService {
    private static final String FOLDER = "comments";
    private final Context ctx;
    private final String lang;
    private final DirAccess dao;
    private final SimpleDirAccess simpledao;
    private final SeiteSO seite;
    private final String dir;
    private final String parentEntityPath;
    private final String title;
    private final String commentsPagePath;
    private final String bbi;
    private final String key;
    
    public SeiteCommentService(Context ctx) {
        this.ctx = ctx;
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String seiteId = ctx.pathParam("id");

        UserSO user = StatesSO.get(ctx).getUser();
        lang = user.getGuiLanguage();
        dao = user.dao();
        WorkspaceSO workspace = user.getWorkspace(branch);
        simpledao = new SimpleDirAccess(dao, workspace);
        seite = workspace.getBooks().byFolder(bookFolder).seiteById(seiteId);
        dir = seite.getBook().getFolder() + "/" + FOLDER + "/" + seite.getId();
        bbi = branch + "/" + bookFolder + "/" + seite.getId();
        key = ":" + branch + ":" + bookFolder + ":" + seite.getId() + ":comment";
        parentEntityPath = "/s/" + bbi;
        commentsPagePath = "/sc/" + bbi + "/comments";
        title = seite.getTitle();
    }
    
    @Override
    protected SimpleDirAccess dao() {
        return simpledao;
    }
    
    @Override
    protected String dir() {
        return dir;
    }
    
    @Override
    public void save(Comment comment, String commitMessage) {
        simpledao.save(comment.getId(), comment, CommentImageUploadService.popImages(comment.getId()),
                new CommitMessage(seite, commitMessage), dir);
    }

    @Override
    public void delete() {
        simpledao.delete(ctx.queryParam("id"), new CommitMessage(seite, "comment deleted"), dir);
    }

    @Override
    public void initModel(DataMap m) {
        m.put("N", "en".equals(lang) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
        m.put("parentEntityTitle", Escaper.esc(title));
        m.put("parentEntityPath", parentEntityPath);
        m.put("addlink", "/sc/" + bbi + "/comment");
        String id = m.get("id").toString();
        m.put("postcontentslink", "/post-contents/comment?key=" + u(getKey(id))); 
        m.put("imageuploadlink", "/image-upload/" + bbi + "/comment?id=" + u(id));
    }
    
    @Override
    public void initModel(Comment c, DataMap m) {
        m.put("N", "en".equals(lang) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
        String path = "/sc/" + bbi + "/comment";
        m.put("replylink", path + "?parent=" + c.getId());
        m.put("editlink", path + "?id=" + c.getId());
        m.put("donelink", path + "-done?id=" + c.getId());
        m.put("deletelink", "/sc/" + bbi + "/delete-comment?id=" + c.getId());
    }

    @Override
    public String getCommentsPagePath() {
        return commentsPagePath;
    }

    @Override
    public String getLogin() {
        return seite.getLogin();
    }
    
    @Override
    public String getLanguage() {
        return lang;
    }

    @Override
    public String getKey(String commentId) {
        return commentId + key;
    }

    @Override
    public String getParentShortId() {
        return seite.getId();
    }
    
    private String u(String text) {
        return Escaper.urlEncode(text, "");
    }
}
