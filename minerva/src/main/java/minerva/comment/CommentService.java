package minerva.comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import com.github.template72.data.DataMap;
import com.google.gson.Gson;

import github.soltaufintel.amalia.spark.Context;
import minerva.access.MultiPurposeDirAccess;
import minerva.access.SimpleDirAccess;

public abstract class CommentService {
    /** key: path Predicate, value: CommentService class */
    public static final Map<Predicate<Context>, Class<? extends CommentService>> services = new HashMap<>();
    protected SimpleDirAccess dao;

    public static CommentService service(Context ctx) {
        for (Entry<Predicate<Context>, Class<? extends CommentService>> e : services.entrySet()) {
            if (e.getKey().test(ctx)) {
                try {
                    return e.getValue().getConstructor(Context.class).newInstance(ctx);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("There's no CommentService für this request.");
    }

    protected abstract SimpleDirAccess dao();
    
    protected abstract String dir();

    public List<Comment> getComments() {
        Map<String, String> files = dao().dao().loadAllFiles(dir());
        
        List<Comment> allComments = new ArrayList<>();
        Gson gson = new Gson();
        for (Entry<String, String> e : files.entrySet()) {
            allComments.add(gson.fromJson(e.getValue(), Comment.class));
        }

        List<Comment> ret = new ArrayList<>();
        addLoadedComments("", allComments, ret);
        return ret;
    }

    private void addLoadedComments(String parentId, List<Comment> allComments, List<Comment> result) {
        Iterator<Comment> iter = allComments.iterator();
        while (iter.hasNext()) {
            Comment comment = iter.next();
            if (comment.getParentId().equals(parentId)) {
                result.add(comment);
                addLoadedComments(comment.getId(), allComments, comment.getComments()); // recursive
                result.sort((a, b) -> a.getCreated().compareTo(b.getCreated()));
            }
        }
    }
    
    public Comment get(String id) {
        Comment ret = new MultiPurposeDirAccess(dao().dao()).load(dir() + "/" + id + ".json", Comment.class);
        if (ret == null) {
            throw new RuntimeException("Comment does not exist!");
        }
        return ret;
    }
    
    public abstract void save(Comment comment, String commitMessage);
    
    public abstract void delete();

    public abstract void initModel(DataMap model);

    public abstract void initModel(Comment comment, DataMap model);

    public abstract String getCommentsPagePath();
    
    public abstract String getLogin();
}
