package minerva.model;

import java.util.Comparator;

import minerva.base.Version;

public class SeiteTitleComparator implements Comparator<SeiteSO> {
    private final boolean reversedOrder;
    private String language;

    public SeiteTitleComparator(boolean reversedOrder) {
        this.reversedOrder = reversedOrder;
    }

    @Override
    public int compare(SeiteSO a, SeiteSO b) {
        String aa, bb;
        if (language == null) {
            // sort by current user language
            aa = a.getSortTitle();
            bb = b.getSortTitle();
        } else {
            aa = a.getSortTitle(language);
            bb = b.getSortTitle(language);
        }
        if (reversedOrder) {
            aa = Version.version.version(aa);
            bb = Version.version.version(bb);
        }
        int ret = aa.compareTo(bb);
        if (reversedOrder) {
            ret *= -1;
        }
        return ret;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
