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
import minerva.config.MinervaOptions;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.user.User;
import minerva.user.UserAccess;

public abstract class CommonCommentService extends CommentService {
    protected final Context ctx;
    protected final String branch;
    protected final String lang;
    protected final DirAccess dao;
    protected final SimpleDirAccess simpledao;
    protected String dir;
    protected String parentEntityPath;
    protected String title;
    protected String commentsPagePath;
    protected String bbi;
    protected String key;
    
    public CommonCommentService(Context ctx) {
        this.ctx = ctx;
        branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");

        UserSO user = StatesSO.get(ctx).getUser();
        lang = user.getGuiLanguage();
        dao = user.dao();
        WorkspaceSO workspace = user.getWorkspace(branch);
		MinervaWebapp.factory().getBackendService().uptodatecheck(workspace, () -> Logger.info("CommonCommentService #" + id + " -> pull"));
        simpledao = new SimpleDirAccess(dao, workspace);

        init(workspace, bookFolder, id);
    }
    
    protected abstract void init(WorkspaceSO workspace, String bookFolder, String id);
    
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
                getSaveCommitMessage(commitMessage), dir);
        if (changed) {
            sendNotifications(comment.getId(), comment.getPerson());
        }
    }
    
    protected abstract CommitMessage getSaveCommitMessage(String commitMessage);

    protected void sendNotifications(String commentId, String person/*login*/) {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        if (!c.readyForCommentNotifications()) {
            Logger.info("send no mails because there's no mail configuration");
        } else if (!person.isEmpty()) {
            String login = getLogin();
            if (person.equals(login)) {
                Logger.debug("don't send note notification mail to myself");
                return;
            }
            User user = UserAccess.loadUser(person);
            String ea = user == null ? null : user.getMailAddress();
            if (StringService.isNullOrEmpty(ea)) {
                Logger.warn("don't send note notification mail because there's no mail address for " + person);
            } else {
                String path = commentsPagePath + "?highlight=" + commentId + "#" + commentId;
                String myTasksPath = "/w/" + branch + "/my-tasks";
                Mail mail = new Mail();
                mail.setSubject(MinervaOptions.MAIL_COMMENT_SUBJECT.get()
                        .replace("{pageTitle}", title)); // no esc!
                mail.setBody(c.getCommentBody()
                        .replace("{pageTitle}", title) // no esc!
                        .replace("{commentPath}", path)
                        .replace("{myTasksPath}", myTasksPath));
                mail.setToEmailaddress(ea);
                c.sendMail(mail);
            }
        }
    }

    @Override
    public void delete() {
        simpledao.delete(ctx.queryParam("id"), getDeleteCommitMessage(), dir);
    }

    protected abstract CommitMessage getDeleteCommitMessage();

    @Override
    public void initModel(DataMap m) {
        m.put("N", "en".equals(lang) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
        m.put("parentEntityTitle", Escaper.esc(title));
        m.put("parentEntityPath", parentEntityPath);
        m.put("addlink", "/" + getPathPrefix() + "/" + bbi + "/comment");
        String id = m.get("id").toString();
        m.put("postcontentslink", "/post-contents/comment?key=" + u(getKey(id)));
        m.put("imageuploadlink", "/image-upload/" + bbi + "/comment?id=" + u(id));
        m.put("bigEditor", false);
        m.put("saveError", NLS.get(lang, "saveCommentError"));
        
        DataMap map = m.list("languages").add();
        map.put("LANG", "");
        map.put("lang", "");
        map.put("editorLanguage", lang);
        map.put("onloadExtra", "");
    }

    @Override
    public void initModel(Comment c, DataMap m) {
        m.put("N", "en".equals(lang) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
        String path = "/" + getPathPrefix() + "/" + bbi + "/comment";
        m.put("replylink", path + "?parent=" + c.getId());
        m.put("editlink", path + "?id=" + c.getId());
        m.put("donelink", path + "-done?id=" + c.getId());
        m.put("deletelink", "/" + getPathPrefix() + "/" + bbi + "/delete-comment?id=" + c.getId());
    }
    
    protected abstract String getPathPrefix();
    
    @Override
    public String getCommentsPagePath() {
        return commentsPagePath;
    }

    @Override
    public String getLanguage() {
        return lang;
    }

    @Override
    public String getKey(String commentId) {
        return commentId + key;
    }
    
    private String u(String text) {
        return Escaper.urlEncode(text, "");
    }
}
