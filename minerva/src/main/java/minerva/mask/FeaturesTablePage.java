package minerva.mask;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class FeaturesTablePage extends SPage {
    MaskAndDataFields mad0;
    
    @Override
    protected void execute() {
        if (isPOST()) {
            filter();
        } else {
            header(seite.getTitle());
            menu();
            if (!seite.getSeiten().isEmpty()) {
                mad0 = new MaskAndDataFields(seite.getSeiten().get(0));
            }
            searchFields();
            columns();
            rows();
        }
    }

    private void menu() {
        model.put("hasBook", false);
        model.put("hasMenuItems", true);
        DataList list2 = model.list("menuItems");
        DataMap map = list2.add();
        map.put("link", "/b/" + branch + "/" + seite.getBook().getBook().getFolder());
        map.put("title", seite.getBook().getTitle());
    }

    private void searchFields() {
        DataList list = list("searchFields");
        if (mad0 != null) {
            final String st = """
                    <label for="{id}" class="col-lg-1 control-label">{label}</label>
                    <div class="col-lg-2">
                        <input class="form-control" type="text" id="{id}" name="{id}">
                    </div>
                     """;
            List<MaskField> fields = mad0.getMaskFields();
            int n = fields.size();
            for (int i = 0; i < n; i += 3) {
                String html = "";
                for (int j = 0; j <= 2; j++) {
                    MaskField f = fields.get(i + j);
                    html += st.replace("{label}", f.getLabel()).replace("{id}", f.getId());
                }
                list.add().put("html", html);
            }
        }
    }

    private void columns() {
        String columns = "";
        if (mad0 != null) {
            for (MaskField f : mad0.getMaskFields()) {
                columns += "<th>" + f.getLabel() + "</th>";
            }
        }
        put("columns", columns);
    }
    
    private void rows() {
        DataList list = list("features");
        for (SeiteSO ft : seite.getSeiten()) {
            String columns = "";
            MaskAndDataFields mad = new MaskAndDataFields(ft);
            
            boolean drin = true;
            for (MaskField f : mad.getMaskFields()) {
                String v = ctx.formParam(f.getId());
                if (!StringService.isNullOrEmpty(v)
                        && !mad.getDataFields().get(f.getId()).toLowerCase().contains(v.toLowerCase())) {
                    drin = false;
                    break;
                }
            }
            if (!drin) {
                continue;
            }
            
            boolean first = true;
            for (MaskField f : mad.getMaskFields()) {
                if (first) {
                    columns += "<td><a href=\"/s/" + branch + "/" + book.getBook().getFolder() + "/" + ft.getId()
                            + "\">" + mad.getDataFields().get(f.getId()) + "</a></td>";
                } else {
                    columns += "<td>" + mad.getDataFields().get(f.getId()) + "</td>";
                }
                first = false;
            }
            DataMap row = list.add();
            row.put("columns", columns);
        }
        putInt("n", seite.getSeiten().size());
    }

    private void filter() {
        String q = "";
        if (!seite.getSeiten().isEmpty()) {
            String p = "?";
            for (MaskField f : new MaskAndDataFields(seite.getSeiten().get(0)).getMaskFields()) {
                String v = ctx.formParam(f.getId());
                if (!StringService.isNullOrEmpty(v)) {
                    q += p + f.getId() + "=" + u(v);
                    p = "&";
                }
            }
        }
        ctx.redirect("/f/" + branch + "/" + bookFolder + "/" + seite.getId() + q);
    }
}
