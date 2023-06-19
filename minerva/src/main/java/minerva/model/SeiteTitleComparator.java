package minerva.model;

import java.util.Comparator;

public class SeiteTitleComparator implements Comparator<SeiteSO> {
    private String language;
    
    @Override
    public int compare(SeiteSO a, SeiteSO b) {
        if (language == null) {
            // sort by current user language
            return a.getSortTitle().compareTo(b.getSortTitle());
        } else {
            return a.getSortTitle(language).compareTo(b.getSortTitle(language));
        }
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
