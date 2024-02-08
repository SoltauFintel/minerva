package minerva.comment;

import java.util.List;

import minerva.access.SimpleDirAccess;
import minerva.base.StringService;
import minerva.model.SeiteSO;

/**
 * Seite-specific comments service
 */
public class SeiteCommentService2 {
    private final SeiteSO seite;
    private final SimpleDirAccess simpledao;
    
    public SeiteCommentService2(SeiteSO seite) {
        this.seite = seite;
        simpledao = new SimpleDirAccess(seite);
    }
    
    public List<Comment> getComments() {
        return CommentService.loadComments(dao().dao(), dir());
    }

    public SimpleDirAccess dao() {
        return simpledao;
    }

    public String dir() {
        return SeiteCommentService.calcDir(seite);
    }

    public String getCommentsSizeText(String login) {
        if (StringService.isNullOrEmpty(login)) {
            throw new IllegalArgumentException("login must not be null");
        }
        int open = 0;
        int done = 0;
        String forMe = "";
        for (Comment c : getComments()) {
            if (c.isDone()) {
                done++;
            } else {
                open++;
                if (login.equals(c.getPerson())) {
                    forMe = "*";
                }
            }
        }
        if (open == 0 && done == 0) {
            return "0";
        }
        return forMe + open + "/" + done;
    }

    /**
     * @param login -
     * @return 0: no open comments, 1: there are open comments, 2: there are open comments for me
     */
    public int getCommentState(String login) {
        if (StringService.isNullOrEmpty(login)) {
            throw new IllegalArgumentException("login must not be null");
        }
        int state = 0;
        for (Comment c : getComments()) {
            if (!c.isDone()) {
                if (login.equals(c.getPerson())) {
                    return 2;
                }
                state = 1;
            }
        }
        return state;
    }
}

