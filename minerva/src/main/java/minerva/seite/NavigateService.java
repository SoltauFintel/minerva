package minerva.seite;

import java.util.List;

import minerva.exclusions.SeiteSichtbar;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class NavigateService {
    private final String lang;
    private final SeiteSichtbar ssc;
    private SeiteSO parent;
    private boolean sortAllowed = true;
    /** null: all pages are valid */
    private List<SeiteSO> validPages = null;

    /**
     * @param lang must be a valid value if omitEmptyPages is true
     * @param exclusions if not null: omit page if not accessible
     */
    public NavigateService(String lang, SeiteSichtbar ssc) {
        this.lang = lang;
        this.ssc = ssc;
    }

    public boolean isSortAllowed() {
        return sortAllowed;
    }

    public void setSortAllowed(boolean sortAllowed) {
        this.sortAllowed = sortAllowed;
    }

    public SeiteSO nextPage(final SeiteSO current) {
        SeiteSO ret;
        // Prio 1: erste untergerodnete Seite
        ret = getFirstSubpage(current);
        if (ret != null) {
            return ret;
        }
        // Prio 2: nächster auf gleicher Ebene
        SeiteSO take = current;
        while (true) {
            ret = nextPageOnSameLevel(take);
            if (ret != null) {
                return ret;
            } else if (parent == null) {
                return current;
            }
            take = parent; // Prio 3: vom parent der nächste
        }
    }
    
    private SeiteSO getFirstSubpage(SeiteSO parent) {
        for (SeiteSO seite : seiten(parent)) {
            if (valid(seite)) {
                return seite;
            }
        }
        return null;
    }

    public SeiteSO previousPage(final SeiteSO current) {
        SeiteSO prev = previousPageOnSameLevel(current);
        if (prev == null) {
            if (parent == null) {
                return current;
            }
            if (validPages != null && !validPages.contains(parent)) {
                return null;
            }
            return parent;
        }
        return lastPage(prev);
    }
    
    private SeiteSO lastPage(final SeiteSO page) {
        SeitenSO seiten = seiten(page);
        for (int i = seiten.size() - 1; i >= 0; i--) {
            SeiteSO s = seiten.get(i);
            if (valid(s)) {
                return lastPage(s); // liefere letzten vom letzten
            }
        }
        return page;
    }
    
    private SeiteSO nextPageOnSameLevel(SeiteSO seite) {
        SeitenSO seiten = seiten(seite.getBook());
        parent = null;
        if (seite.hasParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = seiten(parent);
        }
        boolean pick = false;
        for (SeiteSO s : seiten) {
            if (pick && valid(s)) {
                return s;
            }
            if (s.getId().equals(seite.getId())) {
                pick = true;
            }
        }
        return null;
    }

    private SeiteSO previousPageOnSameLevel(SeiteSO seite) {
        SeitenSO seiten = seiten(seite.getBook());
        parent = null;
        if (seite.hasParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = seiten(parent);
        }
        SeiteSO ret = null;
        for (SeiteSO s : seiten) {
            if (s.getId().equals(seite.getId())) {
                return ret;
            }
            if (valid(s)) {
                ret = s;
            }
        }
        return null;
    }
    
    private boolean valid(SeiteSO seite) {
        if (ssc != null && !ssc.isVisible(seite)) {
            return false;
        }
        if (validPages != null) { // for select-pages-HTML-export
            for (SeiteSO i : validPages) {
                if (i.getId().equals(seite.getId())) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
    
    private SeitenSO seiten(SeiteSO parentSeite) {
        return sortAllowed ? parentSeite.getSeiten(lang) : parentSeite.getSeiten();
    }
    
    private SeitenSO seiten(BookSO book) {
        return sortAllowed ? book.getSeiten(lang) : book.getSeiten();
    }

    public void setValidPages(List<SeiteSO> validPages) {
        this.validPages = validPages;
    }
}
