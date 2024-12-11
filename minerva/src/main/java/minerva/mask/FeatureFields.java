package minerva.mask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gitper.base.StringService;
import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class FeatureFields {
	/** Feature field name for the feature number. */
	public static final String FEATURENUMBER = "featurenumber";
	public static final String ENGLISH_NAME = "englishName";
    private String seiteId;
    /** special mask tag or "ft" */
    private String maskTag;
    /** key: field id, value: field content */
    private final Map<String, String> fields = new HashMap<>();
    private final Set<String> pages = new HashSet<>();
    private final Set<String> tickets = new HashSet<>(); // unused
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
    
    public String getFeatureNumber() {
    	return get(FEATURENUMBER);
    }
    
    public void setFeatureNumber(String featureNumber) {
    	set(FEATURENUMBER, featureNumber);
    }
    
    public void set(String id, String value) {
    	if (FEATURENUMBER.equals(id) && value != null) {
    		value = value.replace(" ", "_").toUpperCase();
    	}
        fields.put(id, value);
    }
    
    /**
     * @param x search expression, must be lowercase
     * @param mlcontext -
     * @return null: not found, otherwise label and field content
     */
    public String search(String x, MaskLabelContext mlcontext) {
        for (Entry<String, String> e : fields.entrySet()) {
            if (e.getValue().toLowerCase().contains(x)) {
            	return mlcontext.getLabel(maskTag, e.getKey()) + ": " + e.getValue();
            }
        }
        return null;
    }
    
    public static class MaskLabelContext {
    	/** key: maskTag, value: Mask */
    	private final Map<String, Mask> maskCache = new HashMap<>();
    	/** key: maskTag:key, value: label (or key) */
    	private final Map<String, String> labelCache = new HashMap<>();
    	private final MasksService sv;
    	
    	public MaskLabelContext(WorkspaceSO workspace) {
    		sv = new MasksService(workspace);
    	}
    	
    	public String getLabel(String maskTag, String key) {
    		String cacheKey = maskTag + ":" + key;
    		String ret = labelCache.get(cacheKey);
    		if (ret == null) {
    			ret = calculateLabel(maskTag, key);
    			labelCache.put(cacheKey, ret);
    		}
    		return ret;
    	}
    	
    	private String calculateLabel(String maskTag, String key) {
            if (!StringService.isNullOrEmpty(maskTag)) {
            	Mask mask = maskCache.get(maskTag);
            	if (mask == null) {
            		mask = sv.getMask(maskTag);
            		maskCache.put(maskTag, mask);
            	}
                if (mask != null) {
                    MaskField f = mask.get(key);
                    if (f == null && !"ft".equals(maskTag)) {
                        f = mask.get("ft");
                    }
                    if (f != null) {
                        return f.getLabel();
                    }
                }
            }
            return key;
    	}
    }

    public Set<String> getPages() {
        return pages;
    }

    /**
     * @deprecated do not use
     * @return -
     */
    public Set<String> getTickets() {
        return tickets;
    }

    public Set<String> getLinks() {
        return links;
    }

	public Map<String, String> getFieldsMap() {
		return fields;
	}
}
