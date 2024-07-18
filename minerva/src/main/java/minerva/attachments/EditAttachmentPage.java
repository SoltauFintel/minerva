package minerva.attachments;

import java.util.stream.Collectors;

import minerva.base.UserMessage;
import minerva.model.AttachmentsSO;
import minerva.seite.SPage;

/**
 * Edit attachment categories
 */
public class EditAttachmentPage extends SPage {

    @Override
    protected void execute() {
        String dn = ctx.pathParam("dn");
        
        AttachmentsSO so = new AttachmentsSO(seite);
        Attachment att = so.get(dn);

        if (isPOST()) {
            save(so, att);
            ctx.redirect(viewlink + "/attachments");
        } else {
            header(n("EditAttachmentCategories"));
            put("dn", esc(att.getFilename()));
            put("categories", att.getCategories().stream().collect(Collectors.joining(", ")));
        }
    }

    private void save(AttachmentsSO so, Attachment att) {
        att.getCategories().clear();
        att.fromString(ctx.formParam("categories"));
        if (att.getCategories().isEmpty()) {
            throw new UserMessage("emptyCategories", user);
        }
        so.saveCategories(att);
    }
}
