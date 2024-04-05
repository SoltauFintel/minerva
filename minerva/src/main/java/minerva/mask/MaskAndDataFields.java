package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.mask.field.MaskField;
import minerva.mask.field.MaskFieldType;
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
        String tag = seite.hasParent() ? seite.getParent().getFeatureTag() : "ft"; // Die Parent-Seite ist maßgeblich!!
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
    
    public void customersMultiselect(DataMap model) {
        DataList list = model.list("customersMultiselect");
        for (MaskField f : maskFields) {
            if (MaskFieldType.CUSTOMERS.equals(f.getType())) {
                list.add().put("id", Escaper.esc(f.getId()));
            }
        }
    }
    
    public void save() {
        sv2.set(seite, dataFields);
    }
    
    public boolean findValue(SeiteSO excludeSeite, String id, String value) {
        return sv2.findValue(excludeSeite, id, value);
    }
}
