package minerva.export.template;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.TableComponent;
import minerva.workspace.WPage;

public class ExportTemplateSetsPage extends WPage {

    @Override
    protected void execute() {
        List<ExportTemplateSet> sets = new ExportTemplatesService(workspace).loadAll();
        
        header(n("exportTemplates"));
        DataList list = list("sets");
        for (ExportTemplateSet set : sets) {
            DataMap map = list.add();
            map.put("id", esc(set.getId()));
            map.put("name", esc(set.getName()));
            map.put("customer", esc(set.getCustomer()));
        }
        List<Col> cols = List.of(
            new Col("Name", "<a href=\"/ets/" + branch + "/edit/{{i.id}}\">{{i.name}}</a>").sortable("name"),
            new Col(n("customer"), "{{i.customer}}").sortable("customer"),
            new Col("", "c3",
                        """
                        <a href="/ets/{{branch}}/add?id={{i.id}}"
                            class="btn btn-default btn-xs" title="{{N.copyExportTemplateSet}}"><i class="fa fa-copy"></i></a>
                        <a href="/ets/{{branch}}/delete/{{i.id}}"
                            class="btn btn-danger btn-xs" title="{{N.deleteExportTemplateSet}}"
                            onclick="return confirm('Export Template Set &quot;{{i.name}}&quot; l&ouml;schen?')"><i class="fa fa-trash"></i></a>
                        """)
        );
        put("table1", new TableComponent("wauto", cols, model, "sets"));
    }
}
