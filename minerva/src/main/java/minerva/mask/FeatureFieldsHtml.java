package minerva.mask;

import java.util.List;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.keyvalue.Values;
import minerva.keyvalue.ValuesSO;
import minerva.mask.FeatureRelationsService.Relation;
import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.user.UserAccess;

/**
 * Generate feature fields mask (HTML)
 */
public class FeatureFieldsHtml {
    private final SeiteSO seite;
    private final boolean editMode;
    
    public FeatureFieldsHtml(SeiteSO seite, boolean editMode) {
        this.seite = seite;
        this.editMode = editMode;
    }

    public String html() {
        if (!seite.isFeatureTree() || seite.isPageInFeatureTree()) {
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
            } else if (first && st.contains("<select ")) {
                st = st.replace("<select ", "<select autofocus ");
                first = false;
            }
            ret += st
                    .replace("{id}", f.getId())
                    .replace("{label}", f.getLabel());
        }
        if (!fields.isEmpty()) {
            if (!editMode) {
                String st = buttons(ff);
                ret += st;
            }
        }
        return ret.replace("{branch}", seite.getBook().getWorkspace().getBranch())
                .replace("{bookFolder}", seite.getBook().getBook().getFolder())
                .replace("{seiteId}", seite.getId());
        // no closing </fieldset></form> !
    }

    private String buttons(FeatureFields ff) {
        String li = "/{branch}/{bookFolder}/{seiteId}";
        String st = "<div class=\"form-group\"><div class=\"col-lg-8 col-lg-offset-2\">"
                + "<a href=\"/ff" + li + "\" class=\"btn btn-warning br\">{editFeatureFields}</a>";
        if (seite.hasFt_tag()) {
            st += "<a href=\"/f" + li + "\" class=\"btn btn-success btn-lg br\"><i class=\"fa fa-table\"></i> " + n("Features") + "</a>";
        }
        st += getAdditionalButtons(seite, ff) + "\n</div></div>\n" + relations(ff);
        return st.replace("{editFeatureFields}", n("editFeatureFieldsBtn"));
    }
    
    protected String getAdditionalButtons(SeiteSO seite, FeatureFields ff) {
    	return "";
    }

    private String getFieldHtml(MaskField f, FeatureFields ff) {
        return (
            switch (f.getType()) {
                case USER ->      userField(f, ff);
                case CUSTOMERS -> customersField(f, ff);
                case BOOL ->      boolField(f, ff);
                case INTEGER ->   integerField(f, ff);
                case TEXTAREA ->  textareaField(f, ff);
                default ->        standardField(f, ff);   // TEXT, UNIQUE
            }
            ).replace("{disabled}", !editMode || f.isImportField() ? " disabled" : "");
    }

    private String userField(MaskField f, FeatureFields ff) {
        String options = "";
        String val = ff.get(f.getId());
        if (StringService.isNullOrEmpty(val)) {
            options += "<option selected></option>\n";
        } else {
            options += "<option></option>\n";
        }
        for (String user : UserAccess.getUserNames()) {
            options += "<option" + (user.equals(val) ? " selected" : "") + ">" + user + "</option>\n";
        }
        return """
            <div class="form-group">
                <label for="{id}" class="col-lg-2 control-label">{label}</label>
                <div class="col-lg-8">
                    <select class="form-control" id="{id}" name="{id}"{disabled}>{options}</select>
                </div>
            </div>
             """
                .replace("{options}", options);
    }

    private String customersField(MaskField f, FeatureFields ff) {
        String options = "";
        String[] val = ff.get(f.getId()).split(",");
        ValuesSO valuesSO = new ValuesSO(seite.getBook().getWorkspace());
        Values values = valuesSO.find("CUSTOMERS");
        if (values != null) {
            for (String customer : values.getValues()) {
                boolean enth = false;
                for (String i : val) {
                    if (customer.equals(i)) {
                        enth = true;
                        break;
                    }
                }
                options += "<option" + (enth ? " selected" : "") + ">" + customer + "</option>\n";
            }
        }
        return """
            <div class="form-group">
                <label for="{id}" class="col-lg-2 control-label">{label}</label>
                <div class="col-lg-8">
                    <select class="form-control" id="{id}" name="{id}" multiple="multiple"{disabled}>{options}</select>
                </div>
            </div>
             """
                .replace("{options}", options);
    }

    private String boolField(MaskField f, FeatureFields ff) {
        return """
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
             """
                .replace("{checked}", "true".equals(ff.get(f.getId())) ? " checked" : "");
    }

    private String integerField(MaskField f, FeatureFields ff) {
        return """
                <div class="form-group">
                    <label for="{id}" class="col-lg-2 control-label">{label}</label>
                    <div class="col-lg-2">
                        <input class="form-control" type="text" id="{id}" name="{id}" value="{value}"{disabled}>
                    </div>
                </div>
                 """
                .replace("{value}", Escaper.esc(ff.get(f.getId())));
    }

    private String textareaField(MaskField f, FeatureFields ff) {
        return """
                <div class="form-group">
                    <label for="{id}" class="col-lg-2 control-label">{label}</label>
                    <div class="col-lg-8">
                        <textarea class="form-control" id="{id}" name="{id}" rows="4"{disabled}>{value}</textarea>
                    </div>
                </div>
                 """
                .replace("{value}", Escaper.esc(ff.get(f.getId())));
    }

    private String standardField(MaskField f, FeatureFields ff) {
        return """
            <div class="form-group">
                <label for="{id}" class="col-lg-2 control-label">{label}</label>
                <div class="col-lg-8">
                    <input class="form-control" type="text" id="{id}" name="{id}" value="{value}"{disabled}>
                </div>
            </div>
             """
            .replace("{value}", Escaper.esc(ff.get(f.getId())));
    }

    private String relations(FeatureFields ff) {
        List<Relation> seiten = new FeatureRelationsService().getRelations(seite, ff);
        if (seiten.isEmpty()) {
            return "";
        }
        return "<p><b>" + n("relations") + "</b></p><ul>"
                + seiten.stream().map(s ->
                    "<li><i class=\"fa {icon}\"></i> <a href=\"{link}\"{target}>{title}</a></li>" //
                        .replace("{icon}", s.getIcon()) //
                        .replace("{link}", s.getLink()) //
                        .replace("{target}", s.getLink().startsWith("http") ? " target=\"_blank\"" : "") //
                        .replace("{title}", Escaper.esc(s.getTitle()))).collect(Collectors.joining())
                + "</ul>";
    }
    
    private String n(String key) {
        return NLS.get(seite.getBook().getWorkspace().getUser().getGuiLanguage(), key);
    }
}
