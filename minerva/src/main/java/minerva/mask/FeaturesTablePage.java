package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.comment.SeiteCommentService2;
import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class FeaturesTablePage extends SPage {
    MaskAndDataFields mad0;
    
    @Override
    protected void execute() {
        if (isPOST()) {
            filterValues();
        } else {
            Logger.info(branch + " | " + user.getLogin() + " | Features table " + id + " " + seite.getTitle());
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
        String options = "<option></option>";
        DataList list = list("searchFields");
        if (mad0 != null) {
            final String st = """
                    <label for="{id}" class="col-lg-1 control-label">{label}</label>
                    <div class="col-lg-2">
                        <input class="form-control" type="text" id="{id}" name="{id}" value="{value}">
                    </div>
                     """;
            List<MaskField> fields = mad0.getMaskFields();
            int n = fields.size();
            for (int i = 0; i < n; i += 3) {
                String html = "";
                for (int j = 0; j <= 2; j++) {
                    if (i + j < fields.size()) {
                        MaskField f = fields.get(i + j);
                        String val = ctx.formParam(f.getId());
                        html += st.replace("{label}", f.getLabel()) //
                                .replace("{id}", f.getId()) //
                                .replace("{value}", val == null ? "" : val);
                        
                        String sel = f.getId().equals(ctx.formParam("sort")) ? " selected" : "";
                        options += "<option value=\"" + f.getId() + "\"" + sel + ">" + esc(f.getLabel()) + "</option>";
                    }
                }
                list.add().put("html", html);
            }
        }
        put("options", options);
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
        String url0 = "/s/" + branch + "/" + book.getBook().getFolder() + "/";
        int k = 0, n = 0;
        for (TableEntry te : getFilteredSortedFeatures()) {
            String columns = "";
            boolean first = true;
            for (MaskField field : te.mad.getMaskFields()) {
                String value = te.mad.getDataFields().get(field.getId());
                if (first) {
                    first = false;
                    if (value.isBlank()) {
                        value = "("  + n("empty") + ")";
                    }
                    String url = url0 + te.feature.getId();
                    columns += "<small>" + field.getLabel() + ": </small>"
                            + "<a href=\"" + url + "\"><b>" + value + "</b></a>";
                } else if (!value.isBlank() && !"SQLJasperReportInformation".equals(value)) {
                    columns += "<br/><small>" + field.getLabel() + ": </small><b>" + value + "</b>";
                }
            }
            DataMap row = list.add();
            row.put("columns", columns);
            row.put("eins", ++k % 4 == 1);
            row.put("hasText", te.feature.hasContentR("de") != 0);
            row.put("hasOpenComments", new SeiteCommentService2(te.feature).getCommentState(te.feature.getLogin()) > 0);
            n++;
        }
        putInt("n", n);
    }

    private List<TableEntry> getFilteredSortedFeatures() {
        List<TableEntry> features = new ArrayList<>();
        for (SeiteSO ft : seite.getSeiten()) {
            TableEntry te = new TableEntry(ft);
            if (te.doFilter()) {
                features.add(te);
            }
        }
        
        String sort = ctx.formParam("sort");
        if (!StringService.isNullOrEmpty(sort)) {
            features.forEach(te -> te.sort = te.mad.getDataFields().get(sort));
            features.sort((a, b) -> a.sort.compareToIgnoreCase(b.sort));
        }
        return features;
    }
    
    private class TableEntry {
        public final SeiteSO feature;
        public final MaskAndDataFields mad;
        public String sort;
        
        public TableEntry(SeiteSO feature) {
            this.feature = feature;
            mad = new MaskAndDataFields(feature);
        }
        
        public boolean doFilter() {
            for (MaskField field : mad.getMaskFields()) {
                String value = ctx.formParam(field.getId());
                if (!StringService.isNullOrEmpty(value)
                        && !mad.getDataFields().get(field.getId()).toLowerCase().contains(value.toLowerCase())) {
                    return false;
                }
            }
            return true;
        }
    }

    private void filterValues() {
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
            if (!StringService.isNullOrEmpty(ctx.formParam("sort"))) {
                q += p + "sort=" + u(ctx.formParam("sort"));
            }
        }
        ctx.redirect("/f/" + branch + "/" + bookFolder + "/" + seite.getId() + q);
    }
}
