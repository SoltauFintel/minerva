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
        List<Responsible> responsibles = new FeatureFieldsService().responsibles(book);
        
        header(n("Responsibilities"));
        DataList list = list("responsibles");
        for (Responsible r : responsibles) {
            DataMap map = list.add();
            map.put("name", esc(r.name));
            map.putInt("n", r.seiten.size());
            DataList list2 = map.list("seiten");
            for (RSeite rs : r.seiten) {
                list2.add()
                	.put("title", esc(rs.title))
                	.put("link", "/s/" + branch + "/" + bookFolder + "/" + rs.seiteId)
                	.put("featurenumber", esc(rs.featureNumber))
                	.putHas("featurenumber", rs.featureNumber);
            }
        }
    }
}
