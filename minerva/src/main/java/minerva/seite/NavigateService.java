package minerva.seite;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class NavigateService {
    private SeiteSO parent;
    
    public SeiteSO nextPage(final SeiteSO current) {
        SeiteSO ret;
        // Prio 1: erste untergerodnete SeiteSO
        SeitenSO seiten = current.getSeiten();
        if (!seiten.isEmpty()) {
            return seiten.get(0);
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
        SeitenSO list = page.getSeiten();
        if (list.isEmpty()) {
            return page;
        }
        return lastPage(list.get(list.size() - 1)); // liefere letzten vom letzten
    }
    
    private SeiteSO nextPageOnSameLevel(SeiteSO seite) {
        SeitenSO seiten = seite.getBook().getSeiten();
        parent = null;
        if (!seite.hasNoParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = parent.getSeiten();
        }
        boolean pick = false;
        for (SeiteSO s : seiten) {
            if (pick) {
                return s;
            }
            if (s.getId().equals(seite.getId())) {
                pick = true;
            }
        }
        return null;
    }

    private SeiteSO previousPageOnSameLevel(SeiteSO seite) {
        SeitenSO seiten = seite.getBook().getSeiten();
        parent = null;
        if (!seite.hasNoParent()) {
            parent = seiten.byId(seite.getSeite().getParentId());
            seiten = parent.getSeiten();
        }
        SeiteSO ret = null;
        for (SeiteSO s : seiten) {
            if (s.getId().equals(seite.getId())) {
                return ret;
            }
            ret = s;
        }
        return null;
    }
}
