package minerva.base;

import github.soltaufintel.amalia.web.action.Page;
import gitper.base.StringService;

public class TosmapInfoPage extends Page {

	@Override
	protected void execute() {
		String key = ctx.queryParam("key");
		
		if (!StringService.isNullOrEmpty(key)) {
			Tosmap.remove(key);
		}
		put("pre", Tosmap.getInfo());
	}
}
