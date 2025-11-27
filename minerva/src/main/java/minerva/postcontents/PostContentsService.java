package minerva.postcontents;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import minerva.comment.CommentPCD;
import minerva.seite.SeitePCD;

public class PostContentsService {
    private static final Object LOCK = new Object();
    /** key: type (Bestandteil der post-contents URL), value: PostContentsData class */
    private static final Map<String, Class<? extends PostContentsData>> pcdClasses = new HashMap<>();
    private static PostContentsData last; // einfach verkettete Liste
    
    static {
        pcdClasses.put("seite", SeitePCD.class);
        pcdClasses.put("comment", CommentPCD.class);
    }

    public void processContent(Context ctx) {
        String type = ctx.pathParam("type");
        Class<? extends PostContentsData> cls = pcdClasses.get(type);
        PostContentsData data;
        try {
            data = (PostContentsData) cls.getConstructor(Context.class).newInstance(ctx);
        } catch (InvocationTargetException e) {
            Logger.error(e.getTargetException());
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        push(data);
        Logger.debug("post contents data: " + data.getClass().getSimpleName() + ", key: " + data.getKey() + ", version: " + data.getVersion());
    }

    private void push(PostContentsData data) {
        synchronized (LOCK) {
            data.setPrevious(last);
            last = data;
        }
    }
    
    public PostContentsData waitForContents(String key, int version) {
        PostContentsData data;
        long max = 1000 * 60 * 2;
        long start = System.currentTimeMillis();
        do {
            data = pop(key, version);
            if (data != null) {
                return data;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupt while waiting for post-contents-data. key: " + key + ", version: " + version, e);
            }
        } while (System.currentTimeMillis() - start < max);
        throw new RuntimeException("Timeout while waiting for post-contents-data."
                + " Please update workspace. key: " + key + ", version: " + version);
    
    }

    private PostContentsData pop(String key, int version) {
        synchronized (LOCK) {
            PostContentsData pick = last;
            PostContentsData vorg = null;
            while (pick != null) {
                if (pick.getVersion() == version && pick.getKey().equals(key)) {
                    // delete item:
                    if (vorg == null) {
                        last = pick.getPrevious();
                    } else {
                        vorg.setPrevious(pick.getPrevious());
                    }
                    // return found item:
                    return pick;
                }
                vorg = pick;
                pick = pick.getPrevious();
            }
        }
        return null; // not found
    }
}
