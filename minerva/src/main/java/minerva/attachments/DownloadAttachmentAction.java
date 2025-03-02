package minerva.attachments;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import github.soltaufintel.amalia.web.route.Route;
import minerva.model.AttachmentsSO;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import ohhtml.downloads.Attachment;

public class DownloadAttachmentAction extends Route<Object> {
    private Object response;

    @Override
    protected void execute() {
        UserSO user = StatesSO.get(ctx).getUser();
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");
        String dn = ctx.pathParam("dn");

        SeiteSO seite = user.getWorkspace(branch).getBooks().byFolder(bookFolder).seiteById(id);
        AttachmentsSO so = new AttachmentsSO(seite);
        Attachment att = so.get(dn);

        if (att.getFilename().toLowerCase().endsWith(".pdf")) {
            ctx.contentTypePDF();
        }
        ctx.maxAge(15); // minutes

        File file = new File(seite.getBook().getFolder() + "/attachments/" + seite.getId(), att.getFilename());
        try {
            response = Files.toByteArray(file);
        } catch (IOException e) {
            throw new RuntimeException("Error loading attachment " + att.getFilename(), e);
        }
    }

    @Override
    protected Object render() {
        return response;
    }
}
