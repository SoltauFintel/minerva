package minerva.seite.tag;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.workspace.WPage;

/**
 * Listet alle Seiten aller Bücher zu einem tag
 */
public class TagWPage extends WPage {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");

        List<SeiteSO> seiten = workspace.findTag(tag);

        DataList list = list("seiten");
        for (SeiteSO seite : seiten) {
            BookSO book = seite.getBook();

            DataMap map = list.add();
            map.put("seite", seite.getTitle());
            String bb = branch + "/" + book.getBook().getFolder();
            map.put("seitelink", "/s/" + bb + "/" + seite.getId());
            map.put("book", book.getTitle());
            map.put("booklink", "/b/" + bb);

            DataList list2 = map.list("tags");
            seite.getSeite().getTags().stream().sorted().forEach(tagx -> {
                DataMap map2 = list2.add();
                map2.put("tag", esc(tagx));
                map2.put("link", "/w/" + branch + "/tag/" + esc(tagx));
            });
        }
        putInt("anzahl", seiten.size());
        header(tag + " (tag)");
    }
}
