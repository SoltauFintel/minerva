package minerva.keyvalue;

import java.util.ArrayList;

import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.workspace.WPage;

public class AddValuesPage extends WPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String key = ctx.formParam("key");
            String title = ctx.formParam("title");
            String values = ctx.formParam("values");
            
            if (StringService.isNullOrEmpty(key) || StringService.isNullOrEmpty(title)) {
                throw new UserMessage("kverror1", workspace);
            }
            
            Values v = new Values();
            v.setKey(key);
            v.setTitle(title);
            saveValues(values, v);
            new ValuesSO(workspace).saveNew(v);
            ctx.redirect("/values/" + branch);
        } else {
            header(n("kvcreatetype"));
            put("hasBook", false);
        }
    }
    
    public static void saveValues(String values, Values v) {
        v.setValues(new ArrayList<>());
        for (String i : values.split("\n")) {
            if (!i.isBlank()) {
                i = i.trim();
                if (!v.getValues().contains(i)) {
                    v.getValues().add(i);
                }
            }
        }
    }
}
