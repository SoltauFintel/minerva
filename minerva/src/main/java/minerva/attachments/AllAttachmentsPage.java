package minerva.attachments;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.Cols;
import github.soltaufintel.amalia.web.table.TableComponent;
import minerva.model.AttachmentsSO;
import minerva.model.AttachmentsSO.Attachment2;
import minerva.workspace.WPage;

public class AllAttachmentsPage extends WPage {

    @Override
    protected void execute() {
        var allAttachments = AttachmentsSO.getAllAttachments(workspace);
        var list = list("allAttachments");
        for (Attachment2 att : allAttachments) {
            var map = list.add();
            map.put("title", esc(att.getSeiteTitle()));
            map.put("dn", esc(att.getFilename()));
            map.put("link", esc(att.getLink()));
        }
        var cols = Cols.of(
                Col.si("Seite", "title", "{{i.link}}/attachments"),
                Col.si("Dateiname", "dn"));
        put("table", new TableComponent("wauto", cols, model, "allAttachments"));
        header(n("AllAttachments"));
        Logger.info(user.getLogin() + " | all attachments");
    }
}
