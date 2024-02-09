package minerva.comment;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.mail.Mail;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.access.SimpleDirAccess;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.config.MinervaConfig;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.user.User;
import minerva.user.UserAccess;

/**
 * Seite-specific comments, Context-specific service.
 */
public class SeiteCommentService extends CommentService {
    public static final String FOLDER = "comments";
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
        dir = calcDir(seite);
        bbi = branch + "/" + bookFolder + "/" + seite.getId();
        key = ":" + branch + ":" + bookFolder + ":" + seite.getId() + ":comment";
        parentEntityPath = "/s/" + bbi;
        commentsPagePath = "/sc/" + bbi + "/comments";
        title = seite.getTitle();
    }
    
    public static String calcDir(SeiteSO seite) {
        return seite.getBook().getFolder() + "/" + FOLDER + "/" + seite.getId();
    }

    @Override
    protected SimpleDirAccess dao() {
        return simpledao;
    }
    
    @Override
    public String dir() {
        return dir;
    }

    @Override
    public void save(Comment comment, String commitMessage, boolean add, boolean changed) {
        simpledao.save(comment.getId(), comment, CommentImageUploadService.popImages(comment.getId()),
                new CommitMessage(seite, commitMessage), dir);
        if (changed) {
            sendNotifications(comment.getId(), comment.getPerson());
        }
    }

    private void sendNotifications(String commentId, String person/*login*/) {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        if (!c.readyForCommentNotifications()) {
            Logger.info("send no mails because there's no mail configuration");
        } else if (!person.isEmpty()) {
//TODO            String login = seite.getLogin();
//            if (person.equals(login)) {
//                Logger.debug("don't send note notification mail to myself");
//                return;
//            }
            User user = UserAccess.loadUser(person);
            String ea = user == null ? null : user.getMailAddress();
            if (StringService.isNullOrEmpty(ea)) {
                Logger.warn("don't send note notification mail because there's no mail address for " + person);
            } else {
                BookSO book = seite.getBook();
                String branch = book.getWorkspace().getBranch();
                String path = "/sc/" + branch + "/" + book.getBook().getFolder() + "/" + seite.getId() + "/comments?highlight=" + commentId + "#" + commentId;
                String myTasksPath = "/w/" + branch + "/my-tasks";
                Mail mail = new Mail();
                mail.setSubject(c.getCommentSubject());
                mail.setBody(c.getCommentBody()
                        .replace("{pageTitle}", seite.getTitle()) // no esc!
                        .replace("{commentPath}", path)
                        .replace("{myTasksPath}", myTasksPath));
                mail.setToEmailaddress(ea);
                c.sendMail(mail);
            }
        }
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
