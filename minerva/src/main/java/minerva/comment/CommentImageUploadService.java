package minerva.comment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import github.soltaufintel.amalia.spark.Context;
import minerva.seite.SeiteImageUploadService;

/**
 * Seite-specific comment ImageUploadService
 */
public class CommentImageUploadService extends SeiteImageUploadService {
    private static final String handle = "images";
    /** key: comment ID, value: filenames */
    public static final Map<String, Set<String>> images = new HashMap<>();
    private final String commentId;
    
    public CommentImageUploadService(Context ctx) {
        super(ctx);
        commentId = ctx.queryParam("id");
    }

    @Override
    protected String getFolder() {
        return SeiteCommentService.calcDir(seite);
    }

    @Override
    public void setSubmittedFileName(String submittedFilename) {
        filename = "img/" + commentId + "/" + submittedFilename;
    }
    
    @Override
    public void success() {
        synchronized (handle) {
            Set<String> list = images.get(commentId);
            if (list == null) {
                images.put(commentId, list = new HashSet<>());
            }
            list.add(filename);
        }
    }
    
    public static Set<String> popImages(String pCommentId) {
        synchronized (handle) {
            Set<String> list = images.get(pCommentId);
            if (list != null) {
                images.remove(pCommentId);
                return list;
            }
            return Set.of();
        }
    }
}
