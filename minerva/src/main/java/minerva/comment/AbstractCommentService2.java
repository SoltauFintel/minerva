package minerva.comment;

import java.util.List;

import minerva.access.SimpleDirAccess;
import minerva.base.StringService;

public abstract class AbstractCommentService2 {
    private final SimpleDirAccess simpledao;
    private final String dir;
    
    public AbstractCommentService2(SimpleDirAccess simpledao, String dir) {
        this.simpledao = simpledao;
        this.dir = dir;
    }

    public List<Comment> getComments() {
        return CommentService.loadComments(dao().dao(), dir());
    }

    public SimpleDirAccess dao() {
        return simpledao;
    }

    public String dir() {
        return dir;
    }
    
    public String getCommentsSizeText(String login) {
        if (StringService.isNullOrEmpty(login)) {
            throw new IllegalArgumentException("login must not be null");
        }
        CommentsSize cs = getCommentsSize(getComments(), login);
        if (cs.open == 0 && cs.done == 0) {
            return "0";
        }
        return (cs.forMe ? "*" : "") + cs.open + "/" + cs.done;
    }
    
    private CommentsSize getCommentsSize(List<Comment> comments, String login) {
        CommentsSize ret = new CommentsSize();
        for (Comment c : comments) {
            if (c.isDone()) {
                ret.done++;
            } else {
                ret.open++;
                if (login.equals(c.getPerson())) {
                    ret.forMe = true;
                }
            }
            CommentsSize cs = getCommentsSize(c.getComments(), login); // recursive
            ret.done += cs.done;
            ret.open += cs.open;
            if (cs.forMe) {
                ret.forMe = true;
            }
        }
        return ret;
    }
    
    private static class CommentsSize {
        int open = 0, done = 0;
        boolean forMe = false;
    }

    /**
     * @param login -
     * @return 0: no open comments, 1: there are open comments, 2: there are open comments for me
     */
    public int getCommentState(String login) {
        if (StringService.isNullOrEmpty(login)) {
            throw new IllegalArgumentException("login must not be null");
        }
        return getCommentState(getComments(), login);
    }
    
    private int getCommentState(List<Comment> comments, String login) {
        int state = 0;
        for (Comment c : comments) {
            if (!c.isDone()) {
                if (login.equals(c.getPerson())) {
                    return 2;
                }
                state = 1;
            }
            int sub = getCommentState(c.getComments(), login); // recursive
            if (sub == 2) {
                return sub;
            } else if (sub == 1) {
                state = sub;
            }
        }
        return state;
    }
}
