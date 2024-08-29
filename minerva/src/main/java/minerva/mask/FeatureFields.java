package minerva.mask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import minerva.base.StringService;
import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class FeatureFields {
	/** Feature field name for the feature number. */
	public static final String FEATURENUMBER = "featurenumber";
    // TODO FeatureFields löschen, wenn Seite gelöscht wird
    private String seiteId;
    /** special mask tag or "ft" */
    private String maskTag;
    /** key: field id, value: field content */
    private final Map<String, String> fields = new HashMap<>();
    private final Set<String> pages = new HashSet<>();
    private final Set<String> tickets = new HashSet<>();
    private final Set<String> links = new HashSet<>();
    
    /** Must only be called by FeatureFields.create(feature)! */
    public FeatureFields() {
    }
    
    public static FeatureFields create(SeiteSO feature) {
        FeatureFields ff = new FeatureFields();
        ff.setSeiteId(feature.getId());
        ff.setMaskTag(feature.getFeatureTag());
        return ff;
    }
    
    public String getSeiteId() {
        return seiteId;
    }

    public void setSeiteId(String seiteId) {
        this.seiteId = seiteId;
    }

    public String getMaskTag() {
        return maskTag;
    }

    public void setMaskTag(String maskTag) {
        this.maskTag = maskTag;
    }

    public String get(String id) {
        String ret = fields.get(id);
        return ret == null ? "" : ret;
    }
    
    public void set(String id, String value) {
    	if (FEATURENUMBER.equals(id) && value != null) {
    		value = value.replace(" ", "_").toUpperCase();
    	}
        fields.put(id, value);
    }
    
    /**
     * @param x search expression, must be lowercase
     * @param workspace -
     * @return null: not found, otherwise label and field content
     */
    public String search(String x, WorkspaceSO workspace) {
        for (Entry<String, String> e : fields.entrySet()) {
            if (e.getValue().toLowerCase().contains(x)) {
                MasksService sv = new MasksService(workspace);
                if (!StringService.isNullOrEmpty(maskTag)) {
                    Mask mask = sv.getMask(maskTag);
                    if (mask != null) {
                        MaskField f = mask.get(e.getKey());
                        if (f == null && !"ft".equals(maskTag)) {
                            f = mask.get("ft");
                        }
                        if (f != null) {
                            return f.getLabel() + ": " + e.getValue();
                        }
                    }
                }
                return e.getKey() + ": " + e.getValue();
            }
        }
        return null;
    }

    public Set<String> getPages() {
        return pages;
    }

    public Set<String> getTickets() {
        return tickets;
    }

    public Set<String> getLinks() {
        return links;
    }
}
