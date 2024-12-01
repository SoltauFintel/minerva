package minerva.export.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.base.TableComponent;
import minerva.base.TableComponent.Col;
import minerva.workspace.WPage;

public class ExportTemplateSetsPage extends WPage {

    @Override
    protected void execute() {
        List<ExportTemplateSet> sets = new ExportTemplatesService(workspace).loadAll();
        
        header(n("exportTemplates"));
        List<Map<String, String>> list = new ArrayList<>();
        for (ExportTemplateSet set : sets) {
        	Map<String, String> map = new HashMap<>();
			map.put("id", esc(set.getId()));
			map.put("name", esc(set.getName()));
			map.put("customer", esc(set.getCustomer()));
        	list.add(map);
        }
        Map<String, String> global = Map.of("branch", branch,
        		"N.copyExportTemplateSet", n("copyExportTemplateSet"),
        		"N.deleteExportTemplateSet", n("deleteExportTemplateSet"));
        List<Col> cols = List.of(
	        new Col("Name", "<a href=\"/ets/" + branch + "/edit/{{id}}\">{{name}}</a>").sortable("name"),
	        new Col(n("customer"), "{{customer}}").sortable("customer"),
	        new Col("", "c3",
	        		    """
                        <a href="/ets/{{branch}}/add?id={{id}}"
                            class="btn btn-default btn-xs" title="{{N.copyExportTemplateSet}}"><i class="fa fa-copy"></i></a>
                        <a href="/ets/{{branch}}/delete/{{id}}"
                            class="btn btn-danger btn-xs" title="{{N.deleteExportTemplateSet}}"
                            onclick="return confirm('Export Template Set &quot;{{name}}&quot; l&ouml;schen?')"><i class="fa fa-trash"></i></a>
	        			""")
        );
        put("sets", new TableComponent("wauto", global, cols, list));
    }
}
