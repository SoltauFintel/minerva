package minerva.mask.field;

public class MaskField {
    private String id;
    private String label;
    private boolean importField = false;
    private MaskFieldType type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isImportField() {
        return importField;
    }

    public void setImportField(boolean importField) {
        this.importField = importField;
    }

    public MaskFieldType getType() {
        return type;
    }

    public void setType(MaskFieldType type) {
        this.type = type;
    }
}
