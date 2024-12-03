package minerva.attachments;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.image.Dropzone;
import github.soltaufintel.amalia.web.table.TableComponent;
import github.soltaufintel.amalia.web.table.TableComponent.Col;
import minerva.MinervaWebapp;
import minerva.model.AttachmentsSO;
import minerva.seite.SPage;
import ohhtml.downloads.Attachment;

public class AttachmentsPage extends SPage {

    @Override
    protected void execute() {
        List<Attachment> attachments = new AttachmentsSO(seite).list();

		header(n(getTitleKey()));
        put("cat", esc(user.getAttachmentCategory()));
        put("dropzone", new Dropzone().getHTML(viewlink + "/upload-attachment")
            .replace(" multiple", " ") // TO-DO Wenn man mehr als eine Datei hochlädt, meldet das JS einen Fehler. Auch im Serverlog stehen ominöse Template Fehler.
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
            map.put("cats", att.getCategories().toString());
            map.put("cat1", att.getCategories().isEmpty() ? "" : att.getCategories().get(0));
            map.putHas("cat1", att.getCategories());
        }
        putHas("attachments", list);
        List<Col> cols = List.of(
        		new Col(n("Filename"), "<a href=\"{{viewlink}}/attachments/{{i.filename}}\" target=\"_blank\">{{i.filename}}</a>").sortable("filename"),
        		new Col(n("Categories"), "{{each j in i.categories}}\n"
        				+ "<span class=\"label label-info\">{{j.cat}}</span>\n{{/each}}").sortable("cats"),
        		new Col(n("insertIntoPage"), "{{if i.hasCat1}}${attachment={{i.cat1}}}{{/if}}"),
				new Col("", "tar", """
		                            <a href="{{viewlink}}/edit-attachment/{{i.filename}}" class="btn btn-xs btn-default br"><i
		                                class="fa fa-pencil"></i> {{N.EditCategories}}</a>
		                            <a  onclick="return loeschen('{{i.filename}}', 'n_{{i.id}}');"
		                                href="{{viewlink}}/delete-attachment/{{i.filename}}" class="btn btn-xs btn-danger" title="{{N.delete}}"><i
		                                class="fa fa-trash-o"></i> <i id="n_{{i.id}}" class="fa fa-delicious fa-spin" style="display: none;"></i></a>
						           """)
        		);
        put("table1", new TableComponent(cols, model, "attachments"));
    }
    
    public static String getTitleKey() {
    	return MinervaWebapp.factory().isCustomerVersion() ? "AttachmentsKundenversion" : "Attachments";
    }
}
