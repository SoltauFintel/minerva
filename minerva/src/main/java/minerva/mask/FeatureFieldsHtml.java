package minerva.mask;

import java.util.List;

import minerva.base.NLS;
import minerva.book.BookType;
import minerva.mask.field.MaskField;
import minerva.mask.field.MaskFieldType;
import minerva.model.SeiteSO;

public class FeatureFieldsHtml {
    private final SeiteSO seite;
    private final boolean editMode;
    
    public FeatureFieldsHtml(SeiteSO seite, boolean editMode) {
        this.seite = seite;
        this.editMode = editMode;
    }

    public String html() {
        if (!BookType.FEATURE_TREE.equals(seite.getBook().getBook().getType())) {
            return ""; // feature fields only for feature tree item
        }
        MaskAndDataFields mad = new MaskAndDataFields(seite);
        return mask(mad.getMaskFields(), mad.getDataFields());
    }
    
    private String mask(List<MaskField> fields, FeatureFields ff) {
        String ret = "<form class=\"form-horizontal\"{action}>\n<fieldset>\n"
                .replace("{action}", editMode ? " action=\"/ff/{branch}/{bookFolder}/{seiteId}\" method=\"POST\"" : "");
        boolean first = true;
        for (MaskField f : fields) {
            String st = getFieldHtml(f, ff);
            if (first && st.contains("<input ")) {
                st = st.replace("<input ", "<input autofocus ");
                first = false;
            }
            ret += st
                    .replace("{id}", f.getId())
                    .replace("{label}", f.getLabel());
        }
        if (!fields.isEmpty()) {
            if (editMode) {
                ret += """
                        <div class="form-group">
                            <div class="col-lg-offset-2 col-lg-5">
                                <button type="submit" class="btn btn-primary br" onclick="document.querySelector('#s1').style='';">{save}
                                    <i id="s1" class="fa fa-delicious fa-spin" style="display: none;"></i></button>
                                <a href="/s/{branch}/{bookFolder}/{seiteId}" class="btn btn-default">{cancel}</a>
                            </div>
                        </div>
                        """
                        .replace("{save}", n("save"))
                        .replace("{cancel}", n("cancel"))
                        ;
            } else {
                String st = "<div class=\"form-group\"><div class=\"col-lg-8 col-lg-offset-2\">"
                        + "<a href=\"/ff/{branch}/{bookFolder}/{seiteId}\" class=\"btn btn-primary btn-sm\">{editFeatureFields}</a></div></div>";
                ret += st.replace("{editFeatureFields}", n("editFeatureFields"));
            }
        }
        ret = ret.replace("{branch}", seite.getBook().getWorkspace().getBranch())
                .replace("{bookFolder}", seite.getBook().getBook().getFolder())
                .replace("{seiteId}", seite.getId());
        return ret + "</fieldset>\n</form>\n" + (editMode ? "" : "<hr/>");
    }

    private String getFieldHtml(MaskField f, FeatureFields ff) {
        String st;
        if (MaskFieldType.BOOL.equals(f.getType())) {
            st = """
                <div class="form-group">
                    <div class="col-lg-8 col-lg-offset-2">
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" id="{id}" name="{id}"{disabled}{checked}>
                                {label}
                            </label>
                        </div>
                    </div>
                </div>
                 """;
            st = st.replace("{checked}", "true".equals(ff.get(f.getId())) ? " checked" : "") //
                    .replace("{disabled}", !editMode || f.isImportField() ? " disabled" : "");
        } else if (MaskFieldType.INTEGER.equals(f.getType())) {
            st = """
                    <div class="form-group">
                        <label for="{id}" class="col-lg-2 control-label">{label}</label>
                        <div class="col-lg-2">
                            <input class="form-control" type="text" id="{id}" name="{id}" value="{value}"{disabled}>
                        </div>
                    </div>
                     """
                    .replace("{disabled}", !editMode || f.isImportField() ? " disabled" : "")
                    .replace("{value}", ff.get(f.getId()));
        } else {
            st = """
                <div class="form-group">
                    <label for="{id}" class="col-lg-2 control-label">{label}</label>
                    <div class="col-lg-8">
                        <input class="form-control" type="text" id="{id}" name="{id}" value="{value}"{disabled}>
                    </div>
                </div>
                 """
                .replace("{disabled}", !editMode || f.isImportField() ? " disabled" : "")
                .replace("{value}", ff.get(f.getId()));
        }
        return st;
    }
    
    private String n(String key) {
        return NLS.get(seite.getBook().getWorkspace().getUser().getGuiLanguage(), key);
    }
}
