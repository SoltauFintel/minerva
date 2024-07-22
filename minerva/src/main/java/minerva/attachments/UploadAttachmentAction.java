package minerva.attachments;

import java.io.IOException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.pmw.tinylog.Logger;

import minerva.model.AttachmentsSO;
import minerva.seite.SAction;

public class UploadAttachmentAction extends SAction {
    public static final int MAX_MB = 50;
    
    // TODO Datei gibt es bereits -> wird verhindert, aber es gibt noch keine saubere Meldung für Anwender (oder sowas wie Umbenennen)
    
    @Override
    protected void execute() {
        try {
            ctx.req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("upload"));
            Part part = ctx.req.raw().getPart("file");
            
            long nbytes = part.getSize();
            long mb = nbytes / 1024l / 1024;
            if (mb > MAX_MB) {
                throw new RuntimeException("Attachment too big! Maximum size: " + MAX_MB + " MB");
            }
            String dn = part.getSubmittedFileName();
            Logger.info("UploadAttachmentAction | filename: '" + dn + "', size: " + mb + " MB (" + nbytes + ")");

            new AttachmentsSO(seite).save(part.getInputStream(), dn, user.getAttachmentCategory());

            // nicht nötig -> ctx.redirect(viewlink + "/attachments");
        } catch (IOException | ServletException e) {
            Logger.error(e);
            throw new RuntimeException("Error uploading attachment!");
        }
    }
}
