package minerva.comment;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;
import com.google.gson.Gson;

import github.soltaufintel.amalia.spark.Context;
import gitper.access.DirAccess;
import gitper.access.MultiPurposeDirAccess;
import gitper.access.SimpleDirAccess;

public abstract class CommentService {
    /** key: path Predicate, value: CommentService class */
    public static final Map<Predicate<Context>, Class<? extends CommentService>> services = new HashMap<>();
    protected SimpleDirAccess dao;

    public static CommentService service(Context ctx) {
        for (Entry<Predicate<Context>, Class<? extends CommentService>> e : services.entrySet()) {
            if (e.getKey().test(ctx)) {
                try {
                    return e.getValue().getConstructor(Context.class).newInstance(ctx);
                } catch (InvocationTargetException ex) {
                    Logger.error(ex.getTargetException());
                    throw new RuntimeException(ex);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("There's no CommentService für this request.");
    }

    public List<Comment> getComments() {
        return loadComments(dao().dao(), dir());
    }
    
    public static List<Comment> loadComments(DirAccess dao, String dir) {
        Map<String, String> files = dao.loadAllFiles(dir);
        
        List<Comment> allComments = new ArrayList<>();
        Gson gson = new Gson();
        files.values().forEach(value -> allComments.add(gson.fromJson(value, Comment.class)));

        List<Comment> ret = new ArrayList<>();
        addLoadedComments("", allComments, ret);
        return ret;
    }

    private static void addLoadedComments(String parentId, List<Comment> allComments, List<Comment> result) {
        for (Comment comment : allComments) {
            if (comment.getParentId().equals(parentId)) {
                result.add(comment);
                addLoadedComments(comment.getId(), allComments, comment.getComments()); // recursive
                result.sort((a, b) -> a.getCreated().compareTo(b.getCreated()));
            }
        }
    }
    
    public Comment get(String id) {
        String dn = dir() + "/" + id + ".json";
        Comment ret = new MultiPurposeDirAccess(dao().dao()).load(dn, Comment.class);
        if (ret == null) {
            Logger.error("Comment is null for file: " + dn);
            throw new RuntimeException("Comment does not exist!");
        }
        return ret;
    }
    
    /**
     * @param comment the comment to be saved
     * @param commitMessage commit message for the case that the backend is Git
     * @param add true: new comment, false: comment edited
     * @param changed true: comment is new or is changed (used for sending notification)
     * @param doneParent ID of parent comment if it has to be done, null if parent comment has not to be changed or there is no parent comment
     */
    public abstract void save(Comment comment, String commitMessage, boolean add, boolean changed, String doneParent);
    
    public abstract void delete();

    public abstract void initModel(DataMap model);

    public abstract void initModel(Comment comment, DataMap model);

    protected abstract SimpleDirAccess dao();
    
    /**
     * @return folder where to save the data
     */
    public abstract String dir();

    /**
     * @return link for redirect
     */
    public abstract String getCommentsPagePath();
    
    public abstract String getLogin();
    
    public abstract String getLanguage();

    public abstract String getKey(String id);
    
    public abstract String getParentShortId(); // Seite Id
    
    public abstract void logInfo();
    public abstract void logSaveInfo();
}
