package minerva.export;

import github.soltaufintel.amalia.web.action.IdAndLabel;

public class ExportItem implements IdAndLabel {
    private final String id;
    private final String label;
    
    public ExportItem(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
