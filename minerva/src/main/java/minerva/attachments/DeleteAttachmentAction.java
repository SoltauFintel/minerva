package minerva.attachments;

import minerva.model.AttachmentsSO;
import minerva.seite.SAction;

public class DeleteAttachmentAction extends SAction {

    @Override
    protected void execute() {
        String dn = ctx.pathParam("dn");
        
        new AttachmentsSO(seite).delete(dn);
        
        ctx.redirect(viewlink + "/attachments");
    }
}
