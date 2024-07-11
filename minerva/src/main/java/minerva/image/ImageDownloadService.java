package minerva.image;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import minerva.comment.CommentImageDownloadService;
import minerva.seite.SeiteImageDownloadService;

public abstract class ImageDownloadService {
    public static final Map<Predicate<Context>, Class<? extends ImageDownloadService>> services = new HashMap<>();
    protected File file;
    
    static {
        services.put(ctx -> ctx.path().startsWith("/s/")
                || ctx.path().startsWith("/s-edit/")
                || ctx.path().startsWith("/p/"), SeiteImageDownloadService.class);
        services.put(ctx -> ctx.path().startsWith("/sc/"), CommentImageDownloadService.class);
    }
    
    public static ImageDownloadService get(Context ctx) {
        for (Entry<Predicate<Context>, Class<? extends ImageDownloadService>> e : services.entrySet()) {
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
        throw new RuntimeException("Now ImageDownloadService found for this request!");
    }
    
    protected final String dn(Context ctx) {
        String dn = ctx.pathParam("dn");
        if (dn.contains("..") || dn.contains(":")) { // Angreiferschutz
            Logger.error("[ImageDownloadAction] Illegal filename: " + dn);
            throw new RuntimeException("Illegal filename!");
        }
        return dn;
    }
    
    public final File getDownloadFile() {
        return file;
    }
}
