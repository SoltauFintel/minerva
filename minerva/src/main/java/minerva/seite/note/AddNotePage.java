package minerva.seite.note;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.spark.Context;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.seite.SPage;

public class AddNotePage extends SPage {
    
    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parent");
        if (isPOST()) {
            String text = ctx.formParam("text1");
            List<String> persons = toPersons(ctx);
            seite.notes().addNote(text, persons, parentId);
            ctx.redirect(viewlink + "/notes");
        } else {
            if (StringService.isNullOrEmpty(parentId)) {
                put("parentText", "");
                put("hasParent", false);
            } else {
                put("parentText", esc(seite.notes().find(parentId).getText()));
                put("hasParent", true);
            }
            header(n("addNote"));
            put("parentId", parentId);
            combobox("persons", MinervaWebapp.factory().getPersons(), "", true, model);
        }
    }

    public static List<String> toPersons(Context ctx) {
        List<String> ret = new ArrayList<>();
        String[] persons = ctx.req.queryParamsValues("person");
        for (int i = 0; i < persons.length; i++) {
            if (!persons[i].isBlank()) {
                ret.add(persons[i]);
            }
        }
        return ret;
    }
}
