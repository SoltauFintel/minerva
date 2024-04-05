package minerva.mask;

import java.util.HashMap;
import java.util.Map;

import minerva.model.SeiteSO;

public class FeatureFields {
    // TODO FeatureFields löschen, wenn Seite gelöscht wird
    private String seiteId;
    /** special mask tag or "ft" */
    private String maskTag;
    private final Map<String, String> fields = new HashMap<>();
    
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
        fields.put(id, value);
    }
}
