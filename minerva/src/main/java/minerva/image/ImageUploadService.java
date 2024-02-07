package minerva.image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.spark.Context;
import minerva.base.UserMessage;
import minerva.comment.CommentImageUploadService;
import minerva.model.WorkspaceSO;
import minerva.seite.SeiteImageUploadService;

public abstract class ImageUploadService {
    public static final Map<Predicate<Context>, Class<? extends ImageUploadService>> services = new HashMap<>();
    protected WorkspaceSO workspace;
    protected String filename;

    static {
        services.put(ctx -> ctx.path().startsWith("/s-image-upload/"), SeiteImageUploadService.class);
        services.put(ctx -> ctx.path().endsWith("/comment"), CommentImageUploadService.class);
    }
    
    public static ImageUploadService get(Context ctx) {
        for (Entry<Predicate<Context>, Class<? extends ImageUploadService>> e : services.entrySet()) {
            if (e.getKey().test(ctx)) {
                try {
                    return e.getValue().getConstructor(Context.class).newInstance(ctx);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new RuntimeException("Now ImageUploadService found for this request!");
    }
    
    public void error(String msg) {
        throw new UserMessage(msg, workspace);
    }

    /**
     * set filename
     * @param submittedFilename -
     */
    public abstract void setSubmittedFileName(String submittedFilename);

    public final String getFilename() {
        return filename;
    }
    
    public final File getFile() {
        String dir = getFolder();
        File file = new File(dir, filename);
        if (file.isFile()) { // Name schon vergeben
            int o = filename.lastIndexOf(".");
            if (o >= 0) {
                filename = filename.substring(0, o) + "-" + IdGenerator.createId6() + filename.substring(o);
            } else {
                filename += "-" + IdGenerator.createId6();
            }
            Logger.debug("File name already taken. Changed to " + filename);
            file = new File(dir, filename);
            if (file.isFile()) { // Das sollte doch niemals passieren.
                Logger.error("File already exists: " + file.getAbsolutePath());
                throw new RuntimeException("Error uploading image! Try another filename.");
            }
        }
        file.getParentFile().mkdirs();
        return file;
    }
    
    protected abstract String getFolder();

    /**
     * set filename to object
     */
    public abstract void success();
}
