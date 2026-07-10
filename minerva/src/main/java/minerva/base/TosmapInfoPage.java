package minerva.base;

import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.action.Page;

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
