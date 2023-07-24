package minerva.subscription;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.subscription.BrokenMappingsService.BrokenMapping;
import minerva.workspace.WPage;

public class BrokenMappingsPage extends WPage {

	@Override
	protected void execute() {
		if (!MinervaWebapp.factory().isCustomerVersion()) {
			throw new RuntimeException("Only for customer version");
		}
		List<BrokenMapping> brokenMappings = new BrokenMappingsService(langs).getBrokenMappings(workspace);

		DataList list = list("brokenMappings");
		for (BrokenMapping bm : brokenMappings) {
			DataMap map = list.add();
			map.put("pageTitle", esc(bm.getSeite().getTitle()));
			map.put("pageId", esc(bm.getSeite().getId()));
			map.put("bookFolder", esc(bm.getSeite().getBook().getBook().getFolder()));
			map.put("key", esc(bm.getKey()));
		}
		putInt("n", brokenMappings.size());
		put("empty", brokenMappings.isEmpty());
		header("Broken Mappings");
	}
}
