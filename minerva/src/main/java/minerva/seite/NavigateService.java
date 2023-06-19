package minerva.seite;

import minerva.MinervaWebapp;
import minerva.exclusions.ExclusionsService;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class NavigateService {
    private final boolean omitEmptyPages;
    private final String lang;
    private final ExclusionsService exclusions;
    private SeiteSO parent;

    /**
     * @param omitEmptyPages true: omit empty pages, false: include all pages
     * @param lang must be a valid value if omitEmptyPages is true
     * @param exclusions if not null: omit page if not accessible
     */
    public NavigateService(boolean omitEmptyPages, String lang, ExclusionsService exclusions) {
        if (omitEmptyPages && !MinervaWebapp.factory().getLanguages().contains(lang)) {
            throw new IllegalArgumentException("Argument lang must be specified if omitEmptyPages is true!");
        }
        this.omitEmptyPages = omitEmptyPages;
        this.lang = lang;
        this.exclusions = exclusions;
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
        for (SeiteSO seite : parent.getSeiten(lang)) {
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
            return parent;
        }
        return lastPage(prev);
    }
    
    private SeiteSO lastPage(final SeiteSO page) {
        SeitenSO seiten = page.getSeiten(lang);
        for (int i = seiten.size() - 1; i >= 0; i--) {
            SeiteSO s = seiten.get(i);
            if (valid(s)) {
                return lastPage(s); // liefere letzten vom letzten
            }
        }
        return page;
    }
    
    private SeiteSO nextPageOnSameLevel(SeiteSO seite) {
        SeitenSO seiten = seite.getBook().getSeiten(lang);
        parent = null;
        if (!seite.hasNoParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = parent.getSeiten(lang);
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
        SeitenSO seiten = seite.getBook().getSeiten(lang);
        parent = null;
        if (!seite.hasNoParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = parent.getSeiten(lang);
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
        if (omitEmptyPages && seite.hasContent(lang) == 0) {
            return false;
        }
        if (exclusions != null) {
            exclusions.setSeite(seite);
            if (!exclusions.isAccessible()) {
                return false;
            }
        }
        return true;
    }
}
