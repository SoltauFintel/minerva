package minerva.base;

import github.soltaufintel.amalia.web.action.IdAndLabel;

public final class SimpleIdAndLabel implements IdAndLabel {
    private final String id;
    private final String label;

    public SimpleIdAndLabel(String id, String label) {
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