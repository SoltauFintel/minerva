package minerva.export.template;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

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
	}
}
