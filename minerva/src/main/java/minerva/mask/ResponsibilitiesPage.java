package minerva.mask;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.book.BookPage;
import minerva.mask.FeatureFieldsService.RSeite;
import minerva.mask.FeatureFieldsService.Responsible;

/**
 * Listet auf welche Mitarbeiter für welche Features verantwortlich sind
 */
public class ResponsibilitiesPage extends BPage {

    @Override
    protected void execute() {
        BookPage.oneLang(model, book);
        header(n("Responsibilities"));
        List<Responsible> responsibles = new FeatureFieldsService().responsibles(book);
        DataList list = list("responsibles");
        for (Responsible r : responsibles) {
            DataMap map = list.add();
            map.put("name", esc(r.name));
            map.putInt("n", r.seiten.size());
            DataList list2 = map.list("seiten");
            for (RSeite rs : r.seiten) {
                DataMap map2 = list2.add();
                map2.put("id", esc(rs.seiteId));
                map2.put("title", esc(rs.title));
            }
        }
    }
}
