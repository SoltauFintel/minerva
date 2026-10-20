package minerva.image;

import java.io.File;

import org.pmw.tinylog.Logger;

import com.google.common.io.Files;

import github.soltaufintel.amalia.web.image.AbstractImageDownload;
import github.soltaufintel.amalia.web.image.BinaryData;
import github.soltaufintel.amalia.web.image.IBinaryDataLoader;

public class ImageDownloadAction extends AbstractImageDownload {

    @Override
    protected IBinaryDataLoader getLoader() {
        File file = ImageDownloadService.get(ctx).getDownloadFile();
        if (file != null && !file.isFile()) {
            Logger.error("Image file does not exist: " + file.getAbsolutePath() + " -> Status 404");
            ctx.status(404);
            return () -> BinaryData.NULL;
        }
        try {
            byte[] image = Files.toByteArray(file);
            return () -> new BinaryData(image, file.getName());
        } catch (Exception e) {
            Logger.error("[ImageDownloadAction] (" + (file == null ? null : file.getAbsolutePath()) + ") "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            ctx.status(404);
            return () -> BinaryData.NULL;
        }
    }
}
