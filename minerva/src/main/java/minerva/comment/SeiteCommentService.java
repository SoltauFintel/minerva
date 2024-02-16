package minerva.comment;

import github.soltaufintel.amalia.spark.Context;
import minerva.access.CommitMessage;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

/**
 * Seite-specific comments, Context-specific service.
 */
public class SeiteCommentService extends CommonCommentService {
    public static final String FOLDER = "comments";
    private SeiteSO seite;
    
    public SeiteCommentService(Context ctx) {
        super(ctx);
    }
    
    @Override
    protected void init(WorkspaceSO workspace, String bookFolder, String id) {
        seite = workspace.getBooks().byFolder(bookFolder).seiteById(id);
        title = seite.getTitle();
        dir = calcDir(seite);
        
        bbi = branch + "/" + bookFolder + "/" + id;
        key = ":" + branch + ":" + bookFolder + ":" + id + ":comment";
        parentEntityPath = "/s/" + bbi;
        commentsPagePath = "/" + getPathPrefix() + "/" + bbi + "/comments";
    }
    
    public static String calcDir(SeiteSO seite) {
        return seite.getBook().getFolder() + "/" + FOLDER + "/" + seite.getId();
    }

    @Override
    protected CommitMessage getSaveCommitMessage(String commitMessage) {
        return new CommitMessage(seite, commitMessage);
    }
    
    @Override
    protected CommitMessage getDeleteCommitMessage() {
        return new CommitMessage(seite, "comment deleted");
    }

    @Override
    protected String getPathPrefix() {
        return "sc";
    }
    
    @Override
    public String getLogin() {
        return seite.getLogin();
    }
    
    @Override
    public String getParentShortId() {
        return seite.getId();
    }
}
