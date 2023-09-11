package minerva.model;

import java.util.Comparator;

public class SeiteTitleComparator implements Comparator<SeiteSO> {
    private final boolean reversedOrder;
    private String language;

    public SeiteTitleComparator(boolean reversedOrder) {
        this.reversedOrder = reversedOrder;
    }

    @Override
    public int compare(SeiteSO a, SeiteSO b) {
        int ret;
        if (language == null) {
            // sort by current user language
            ret = a.getSortTitle().compareTo(b.getSortTitle());
        } else {
            ret = a.getSortTitle(language).compareTo(b.getSortTitle(language));
        }
        if (reversedOrder) {
            ret *= -1;
        }
        return ret;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
