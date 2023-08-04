package minerva.validate;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.model.SeiteSO;

public class ValidationPage extends BPage {

    @Override
    protected void execute() {
        int nMessages = 0, nPages = 0;
        header(n("validate"));
        DataList hauptliste = list("hauptliste");
        for (String lang : langs) {
            DataMap langEintrag = hauptliste.add();
            langEintrag.put("lang", lang);
            DataList seiten = langEintrag.list("seiten");
            for (SeiteSO seite : book.getAlleSeiten()) {
                List<String> msg = new ValidatorService().validate(seite, lang, user.getGuiLanguage());
                if (!msg.isEmpty()) {
                    DataMap map = seiten.add();
                    map.put("title", esc(seite.getSeite().getTitle().getString(lang)));
                    map.put("link", "/s/" + esc(branch) + "/" + esc(book.getBook().getFolder()) + "/" + seite.getId());
                    DataList list2 = map.list("fehlerliste");
                    for (String text : msg) {
                        DataMap map2 = list2.add();
                        map2.put("text", text);
                        nMessages++;
                    }
                    map.putInt("n", msg.size());
                    nPages++;
                }
            }
            langEintrag.put("hasEntries", !seiten.isEmpty());
        }
        putInt("nPages", nPages);
        putInt("nMessages", nMessages);
    }
}
