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
        return () -> new BinaryData(null, file.getName()) {
            @Override
            public byte[] getData() {
                try {
                    return Files.toByteArray(file);
                } catch (Exception e) {
                    Logger.error("[ImageDownloadAction] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    return null;
                }
            }
        };
    }
}
