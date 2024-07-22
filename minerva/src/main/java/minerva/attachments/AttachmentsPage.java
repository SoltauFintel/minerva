package minerva.attachments;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.image.Dropzone;
import minerva.model.AttachmentsSO;
import minerva.seite.SPage;
import ohhtml.downloads.Attachment;

public class AttachmentsPage extends SPage {

    @Override
    protected void execute() {
        List<Attachment> attachments = new AttachmentsSO(seite).list();

        header("Attachments");
        put("cat", esc(user.getAttachmentCategory()));
        put("dropzone", new Dropzone().getHTML(viewlink + "/upload-attachment")
            .replace(" multiple", " ") // TODO Wenn man mehr als eine Datei hochlädt, meldet das JS einen Fehler. Auch im Serverlog stehen ominöse Template Fehler.
            );
        putInt("mb", UploadAttachmentAction.MAX_MB);
        DataList list = list("attachments");
        int _id = 0;
        for (Attachment att : attachments) {
            DataMap map = list.add();
            map.put("filename", esc(att.getFilename()));
            map.putInt("id", ++_id);
            // TO-DO Man könnte noch anzeigen, ob die Datei verwendet wird. Das ist insbesondere dann wichtig, wenn eine andere Seite eine Datei referenziert.
            DataList list2 = map.list("categories");
            for (String cat : att.getCategories()) {
                list2.add().put("cat", esc(cat));
            }
        }
        put("hasAttachments", !attachments.isEmpty());
    }
}
