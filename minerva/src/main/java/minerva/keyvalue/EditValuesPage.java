package minerva.keyvalue;

import java.util.stream.Collectors;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.workspace.WPage;

public class EditValuesPage extends WPage {

    @Override
    protected void execute() {
        String key = ctx.pathParam("key");
    
        MinervaWebapp.factory().getBackendService().uptodatecheck(workspace, () -> {});
        ValuesSO so = new ValuesSO(workspace);
        Values v = so.find(key);
        if (isPOST()) {
            String title = ctx.formParam("title");
            String values = ctx.formParam("values");
            if (StringService.isNullOrEmpty(title)) {
                throw new UserMessage("kverror2", workspace);
            }
            v.setTitle(title);
            AddValuesPage.saveValues(values, v);
            so.save(v);
            ctx.redirect("/values/" + branch);
        } else {
			header(n("kvedittype").replace("$t", v.getTitle()));
            put("key", esc(v.getKey()));
            put("title", esc(v.getTitle()));
            put("values", esc(v.getValues().stream().collect(Collectors.joining("\n"))));
            put("hasBook", false);
        }
    }
}
