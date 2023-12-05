package minerva.seite.note;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.spark.Context;
import minerva.base.StringService;
import minerva.seite.Note;
import minerva.seite.SPage;
import minerva.user.UserAccess;

public class AddNotePage extends SPage {
    
    @Override
    protected void execute() {
        String parentId = ctx.queryParam("parent");
        if (isPOST()) {
            String text = ctx.formParam("text1");
            if (!text.isBlank()) {
                List<String> persons = toPersons(ctx);
                seite.notes().addNote(text, persons, parentId);
            }
            ctx.redirect(viewlink + "/notes");
        } else {
            List<String> selectedItems = new ArrayList<>();
            if (StringService.isNullOrEmpty(parentId)) {
                put("parentText", "");
                put("hasParent", false);
            } else {
                Note parentNote = seite.notes().find(parentId);
                put("parentText", esc(parentNote.getText()));
                put("hasParent", true);
                selectedItems.add(parentNote.getUser());
            }
            header(n("addNote"));
            put("parentId", parentId);
            combobox("persons", UserAccess.getUserNames(), selectedItems, true);
        }
    }

    public static List<String> toPersons(Context ctx) {
        List<String> ret = new ArrayList<>();
        String[] persons = ctx.req.queryParamsValues("person");
        for (int i = 0; i < persons.length; i++) {
            if (!persons[i].isBlank()) {
                ret.add(UserAccess.realName2Login(persons[i]));
            }
        }
        return ret;
    }
}
