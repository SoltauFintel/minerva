package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import minerva.mask.field.MaskField;

public class Mask {
    /** Mask id, mask title, tag */
    private String tag;
    private final List<MaskField> fields = new ArrayList<>();

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<MaskField> getFields() {
        return fields;
    }

    public MaskField get(String id) {
        return fields.stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);
    }
}
