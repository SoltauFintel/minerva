package minerva.seite.note;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.data.IDataMap;

import minerva.MinervaWebapp;
import minerva.seite.Note;
import minerva.seite.SPage;

public class EditNotePage extends SPage {

    @Override
    protected void execute() {
        if (ctx.queryParam("number") == null) {
            throw new RuntimeException("Can't display page because parameter number not set.");
        }
        int number = Integer.parseInt(ctx.queryParam("number"));

        Note note = seite.notes().noteByNumber(number);

        if (isPOST()) {
            String text = ctx.formParam("text1");
            List<String> persons = AddNotePage.toPersons(ctx);

            note.setText(text);
            note.getPersons().clear();
            note.getPersons().addAll(persons);
            note.setChanged(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            seite.saveMeta(seite.getTitle() + ": edit note #" + note.getNumber());
            // TODO Nicht speichern wenn sich nix geändert hat.
            
            ctx.redirect(viewlink + "/notes");
        } else {
            header(n("editNote").replace("$no", "" + note.getNumber()));
            putInt("number", note.getNumber());
            put("user", esc(note.getUser()));
            put("created", esc(note.getCreated()));
            put("changed", esc(note.getChanged()));
            put("hasChanged", !note.getChanged().isEmpty());
            put("text1", esc(note.getText()));
            put("editAllowed", note.getUser().equals(seite.getLogin()));
            combobox("persons", MinervaWebapp.factory().getPersons(), "%", true, model);
            for (String name : note.getPersons()) { // TODO Amalia
                DataList j = (DataList) model.get("persons");
                for (IDataMap k : j) {
                    DataMap kk = (DataMap) k;
                    if (kk.get("text").toString().equals(name)) {
                        kk.put("selected", true);
                    }
                }
            }
        }
    }
}
