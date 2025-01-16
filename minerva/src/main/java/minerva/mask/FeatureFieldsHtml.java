package minerva.mask;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.base.StringService;
import minerva.base.NLS;
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
        String html = mask(mad.getMaskFields(), mad.getDataFields());
        html += """
        		<script>
        			function copy(id) {
						document.getElementById(id).select();
						document.execCommand('copy');
					}
				</script>
        		""";
        return html;
    }
    
    private String mask(List<MaskField> fields, FeatureFields ff) {
        String ret = "<form class=\"form-horizontal\"{action}>\n<fieldset>\n"
                .replace("{action}", editMode ? " action=\"/ff/{branch}/{bookFolder}/{seiteId}\" method=\"POST\"" : "");
        boolean first = true;
        for (MaskField f : fields) {
            String st = getFieldHtml(f, ff);
            if (editMode) {
	            if (first && st.contains("<input ")) {
	                st = st.replace("<input ", "<input autofocus ");
	                first = false;
	            } else if (first && st.contains("<select ")) {
	                st = st.replace("<select ", "<select autofocus ");
	                first = false;
	            }
            }
            ret += st
                    .replace("{id}", f.getId())
                    .replace("{label}", f.getLabel());
        }
        if (!fields.isEmpty()) {
            if (!editMode) {
                String st = relationsAndButtons(ff);
                ret += st;
            }
        }
        return ret.replace("{branch}", seite.getBook().getWorkspace().getBranch())
                .replace("{bookFolder}", seite.getBook().getBook().getFolder())
                .replace("{seiteId}", seite.getId());
        // no closing </fieldset></form> !
    }

    private String relationsAndButtons(FeatureFields ff) {
    	
    	// RELATIONS ----
        String st = relations(ff);
        
        // BUTTONS ----
        boolean indent = st.isEmpty();
        if (indent) {
            st += "<div class=\"form-group\"><div class=\"col-lg-8 col-lg-offset-2\">";
        }
        String li = "/{branch}/{bookFolder}/{seiteId}";
        st += "<a href=\"/ff" + li + "\" class=\"btn btn-warning br\"><i class=\"fa fa-sitemap\"></i> {editFeatureFields}</a>";
        if (seite.hasFt_tag()) {
            st += "<a href=\"/f" + li + "\" class=\"btn btn-success btn-lg br ml1\"><i class=\"fa fa-table\"></i> " + n("Features") + "</a>";
        }
        st += getAdditionalButtons(seite, ff);
        if (indent) {
            st += "</div></div>";
        }
        return st.replace("{editFeatureFields}", n("editFeatureFieldsBtn"));
    }
    
    protected String getAdditionalButtons(SeiteSO seite, FeatureFields ff) {
    	return "";
    }

    private String getFieldHtml(MaskField f, FeatureFields ff) {
        String html = (
            switch (f.getType()) {
                case USER ->      userField(f, ff);
                case CUSTOMERS -> customersField(f, ff);
                case BOOL ->      boolField(f, ff);
                case INTEGER ->   integerField(f, ff);
                case TEXTAREA ->  textareaField(f, ff);
                default ->        standardField(f, ff);   // TEXT, UNIQUE
            }
            );
        String d = f.isFeatureNr() ? " readonly" /*damit Copy geht*/ : " disabled";
        return html.replace("{disabled}", !editMode || f.isImportField() ? d : "");
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
        String html = """
            <div class="form-group">
                <label for="{id}" class="col-lg-2 control-label">{label}</label>
                <div class="col-lg-8">{copydiv}
                    <input class="form-control" type="text" id="{id}" name="{id}" value="{value}"{disabled}>{copy}
                </div>
            </div>
             """;
        html = html.replace("{value}", Escaper.esc(ff.get(f.getId())));
        String copy = "", copydiv = "";
        if (!editMode && f.isFeatureNr()) {
        	copydiv = "<div class=\"input-group\">";
        	copy = "<a onclick=\"copy('{id}');\" class=\"btn btn-default btn-sm input-group-addon\" title=\"Copy\"><i class=\"fa fa-copy\"></i></a></div>";
        }
        return html.replace("{copydiv}", copydiv).replace("{copy}", copy);
    }

    private String relations(FeatureFields ff) {
        List<Relation> relations = new FeatureRelationsService().getRelations(seite, ff);
        if (relations.isEmpty()) {
            return "";
        }
        Set<Integer> cols = new TreeSet<>();
        for (Relation r : relations) {
        	cols.add(Integer.valueOf(r.getColumn()));
        }
		StringBuilder top = new StringBuilder();
		StringBuilder bottom = new StringBuilder();
		top.append("<p><b>");
		top.append(n("relations"));
		top.append("</b></p><table class=\"relationstable\"><tr>");
		bottom.append("<tr>");
		for (Integer col : cols) {
			Relation muster = null;
			for (Relation r : relations) {
				if (r.getColumn() == col.intValue()) {
					muster = r;
					break;
				}
			}
			if (muster == null) { // can not happen
				continue;
			}
			top.append("<td>");
			top.append(n(muster.getColumnTitleKey()));
			top.append("</td>");
            bottom.append("<td><ul class=\"ulFeatures\">");
			for (Relation s : relations) {
				if (s.getColumn() == col.intValue()) {
					bottom.append(("<li>" + (s.noBreak() ? "<nobr>" : "") + "<i class=\"fa {icon}\"></i> <a href=\"{link}\"{target}>{title}</a>" //
					        + (s.noBreak() ? "</nobr>" : "") + "</li>") //
		                  .replace("{icon}", s.getIcon()) //
		                  .replace("{link}", s.getLink()) //
		                  .replace("{target}", s.getLink().startsWith("http") ? " target=\"_blank\"" : "") //
		                  .replace("{title}", Escaper.esc(s.getTitle())));
				}				
			}
			bottom.append("</ul></td>\n");
		}
		top.append("</tr>\n");
		bottom.append("</tr></table>");
		top.append(bottom.toString());
		return top.toString();
    }
    
    private String n(String key) {
        return NLS.get(seite.getBook().getWorkspace().getUser().getGuiLanguage(), key);
    }
}
