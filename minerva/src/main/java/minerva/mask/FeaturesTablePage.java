package minerva.mask;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class FeaturesTablePage extends SPage {

    @Override
    protected void execute() {
        header(seite.getTitle());
        String columns = "";
        if (!seite.getSeiten().isEmpty()) {
            MaskAndDataFields mad = new MaskAndDataFields(seite.getSeiten().get(0));
            for (MaskField f : mad.getMaskFields()) {
                columns += "<th>" + f.getLabel() + "</th>";
            }
        }
        put("columns", columns);

        DataList list = list("features");
        for (SeiteSO ft : seite.getSeiten()) {
            DataMap row = list.add();
            columns = "";
            MaskAndDataFields mad = new MaskAndDataFields(ft);
            boolean first = true;
            for (MaskField f : mad.getMaskFields()) {
                if (first) {
                    columns += "<td><a href=\"/s/" + branch + "/" + book.getBook().getFolder() + "/" + ft.getId() + "\">"
                            + mad.getDataFields().get(f.getId()) + "</a></td>";
                }else {
                    columns += "<td>" + mad.getDataFields().get(f.getId()) + "</td>";
                }
                first = false;
            }
            row.put("columns", columns);
        }
        putInt("n", seite.getSeiten().size());
    }
}
