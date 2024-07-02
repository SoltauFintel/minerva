package minerva.image;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.JsonAction;
import minerva.image.ImageUploadAction.Success;
import spark.utils.IOUtils;

public class ImageUploadAction extends JsonAction<Success> {

    @Override
    protected void execute() {
        ImageUploadService sv = ImageUploadService.get(ctx);

        ctx.req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("upload"));
        ctx.res.type("application/json");
        Part part;
        try {
            part = ctx.req.raw().getPart("upload");
        } catch (IOException | ServletException e) {
            Logger.error(e);
            throw new RuntimeException("Error uploading image!");
        }
        if (part.getSize() > 1024l * 1024 * 10) { // 10 MB
            sv.error("error.imageTooBig");
        }
        sv.setSubmittedFileName(part.getSubmittedFileName());

        try (FileOutputStream fos = new FileOutputStream(sv.getFile())) {
            IOUtils.copy(part.getInputStream(), fos);
        } catch (IOException e) {
            Logger.error(e);
            throw new RuntimeException("Error uploading image!");
        }
        
        sv.success();

        // Response must be JSON, containing url field.
        Success ret = new Success();
        ret.setUrl(sv.getFilename()); // relative filename (e.g. branch name cannot be saved!)
        result = ret;
        Logger.info("image saved: " + sv.getFile().toString());
    }

    public static class Success {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
