package minerva.keyvalue;

import java.util.ArrayList;

import minerva.base.StringService;
import minerva.workspace.WPage;

public class AddValuesPage extends WPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String key = ctx.formParam("key");
            String title = ctx.formParam("title");
            String values = ctx.formParam("values");
            
            if (StringService.isNullOrEmpty(key) || StringService.isNullOrEmpty(title)) {
                throw new RuntimeException("Bitte Typ und Titel eingeben!");
            }
            
            Values v = new Values();
            v.setKey(key);
            v.setTitle(title);
            saveValues(values, v);
            new ValuesSO(workspace).saveNew(v);
            ctx.redirect("/values/" + branch);
        } else {
            header("Neuen Schlüsseltyp anlegen");
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
