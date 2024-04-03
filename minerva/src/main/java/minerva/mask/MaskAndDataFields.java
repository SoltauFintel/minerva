package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import minerva.mask.field.MaskField;
import minerva.model.SeiteSO;

/**
 * MaskFields and FeatureFields
 */
public class MaskAndDataFields {
    private final SeiteSO seite;
    private final List<MaskField> maskFields;
    private final FeatureFields dataFields;
    private final FeatureFieldsService sv2 = new FeatureFieldsService();
    
    public MaskAndDataFields(SeiteSO seite) {
        this.seite = seite;
        String tag = seite.getFeatureTag();
        MasksService sv = new MasksService(seite.getBook().getWorkspace());
        maskFields = new ArrayList<>();
        maskFields.addAll(sv.getMask("ft").getFields());
        if (!"ft".equals(tag)) {
            maskFields.addAll(sv.getMask(tag).getFields());
        }
        dataFields = sv2.get(seite);
    }

    public List<MaskField> getMaskFields() {
        return maskFields;
    }

    public FeatureFields getDataFields() {
        return dataFields;
    }
    
    public void save() {
        sv2.set(seite, dataFields);
    }
}
