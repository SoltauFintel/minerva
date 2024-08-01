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
            Logger.info(user.getLogin() + " | " + branch + " | Features table #" + id + ": " + seite.getTitle());
            header(seite.getTitle());
            menu();
            if (!seite.getSeiten().isEmpty()) {
                mad0 = new MaskAndDataFields(seite.getSeiten().get(0));
            }
            searchFields();
            rows();
        }
    }

    private void menu() {
        model.put("hasBook", false);
        model.put("hasMenuItems", true);
        DataList list2 = model.list("menuItems");
        DataMap map = list2.add();
        map.put("link", "/b/" + branch + "/" + seite.getBook().getBook().getFolder());
        map.put("title", esc(seite.getBook().getTitle()));
    }

    private void searchFields() {
        String options = "";
        DataList list = list("searchFields");
        if (mad0 != null) {
            final String st = """
                    <label for="{id}" class="col-lg-1 control-label">{label}</label>
                    <div class="col-lg-2">
                        <input class="form-control" type="text" id="{id}" name="{id}" value="{value}">
                    </div>
                     """;
            List<MaskField> fields = mad0.getMaskFields();
            String val = ctx.formParam("_titel");
            String html = st.replace("{label}", "Feature-Titel") //
                    .replace("{id}", "_titel") //
                    .replace("{value}", val == null ? "" : val);
            String sel = "_titel".equals(ctx.formParam("sort")) ? " selected" : "";
            options += "<option value=\"_titel\"" + sel + ">Feature-Titel</option>";
            int sp = 1;
            int n = fields.size();
            for (int i = 0; i < n; i += 3) {
                for (int j = 0; j <= 2; j++) {
                    if (i + j < fields.size()) {
                        MaskField f = fields.get(i + j);
                        val = ctx.formParam(f.getId());
                        html += st.replace("{label}", f.getLabel()) //
                                .replace("{id}", f.getId()) //
                                .replace("{value}", val == null ? "" : val);
                        sp++;
                        if (sp % 3 == 0) {
                            list.add().put("html", html);
                            html = "";
                            sp = 0;
                        }
                        
                        sel = f.getId().equals(ctx.formParam("sort")) ? " selected" : "";
                        options += "<option value=\"" + f.getId() + "\"" + sel + ">" + esc(f.getLabel()) + "</option>";
                    }
                }
            }
			if (!html.isEmpty()) {
				list.add().put("html", html);
			}
        }
        put("options", options);
    }

    private void rows() {
        DataList list = list("features");
        String url0 = "/s/" + branch + "/" + book.getBook().getFolder() + "/";
        int k = 0, n = 0;
        for (TableEntry te : getFilteredSortedFeatures()) {
			String columns = "";
            for (MaskField field : te.mad.getMaskFields()) {
                String value = te.mad.getDataFields().get(field.getId());
                if (!value.isBlank() && !"SQLJasperReportInformation".equals(value)) {
                    columns += "\n<br/><small>" + field.getLabel() + ": </small><b>" + value + "</b>";
                }
            }
            String title = te.feature.getTitle();
            if (title.isBlank()) {
            	title = "(" + n("empty") + ")";
            }
            DataMap row = list.add();
            row.put("title", esc(title));
            row.put("url", esc(url0 + te.feature.getId()));
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
		String _titel = ctx.queryParam("_titel");
		_titel = _titel == null ? "" : _titel.toLowerCase();
        for (SeiteSO ft : seite.getSeiten()) {
            TableEntry te = new TableEntry(ft);
            if (te.doFilter() && (_titel.isEmpty() || ft.getTitle().toLowerCase().contains(_titel))) {
                features.add(te);
            }
        }
        
        String sort = ctx.formParam("sort");
    	if ("_titel".equals(sort) || StringService.isNullOrEmpty(sort)) {
    		features.forEach(te -> te.sort = te.feature.getTitle().toLowerCase());
    	} else {
    		features.forEach(te -> te.sort = te.mad.getDataFields().get(sort).toLowerCase());
    	}
        features.sort((a, b) -> a.sort.compareTo(b.sort));
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
                String value = ctx.queryParam(field.getId());
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
            String sep = "?";
            String v = ctx.formParam("_titel");
            if (!StringService.isNullOrEmpty(v)) {
                q += sep + "_titel=" + u(v);
                sep = "&";
            }
            for (MaskField f : new MaskAndDataFields(seite.getSeiten().get(0)).getMaskFields()) {
                v = ctx.formParam(f.getId());
                if (!StringService.isNullOrEmpty(v)) {
                    q += sep + f.getId() + "=" + u(v);
                    sep = "&";
                }
            }
            if (!StringService.isNullOrEmpty(ctx.formParam("sort"))) {
                q += sep + "sort=" + u(ctx.formParam("sort"));
            }
        }
        ctx.redirect(seite.getId() + q);
    }
}
