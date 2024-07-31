package minerva.export;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.SeiteSO;

/**
 * Mit dieser Klasse wird versucht die Seitenhierarchie möglichst beizubehalten.
 * Wenn Seite AA eine Unterseite von A ist, so ist AA auch im Export mit Seitenauswahl eine Unterseite von A.
 */
public class SomeSubpages implements SubpagesSelector {
    private final List<SeiteAndDone> allPages;
    
    public SomeSubpages(List<SeiteSO> allPages) {
    	List<SeiteSO> ret = allPages;
		if (allPages.size() == 1) {
			List<String> langs = MinervaWebapp.factory().getLanguages();
			SeiteSO so = allPages.get(0);
			if (so.getBook().isReleaseNotesBook(langs) && so.hasParent() && !so.getParent().hasParent() && !so.getSeiten().isEmpty()) {
				// nice export for single 2nd level page of Release Notes book (e.g. 3.29.x)
				Logger.info("adding subpages for " + so.getTitle());
				ret = new ArrayList<>();
		    	for (SeiteSO sub : so.getSeiten()) {
		    		ret.add(sub);
		    	}
			}			
		}
		this.allPages = ret.stream().map(seite -> new SeiteAndDone(seite)).collect(Collectors.toList());
    }

	public List<SeiteAndDone> getAllPages() {
        return allPages;
    }

    @Override
    public Iterable<SeiteSO> getSubpages(SeiteSO seite) {
        List<SeiteSO> ret = new ArrayList<>();
        for (SeiteSO s : seite.getSeiten()) {
            if (contains(s)) {
                ret.add(s);
            }
        }
        return ret;
    }

    private boolean contains(SeiteSO x) {
        for (SeiteAndDone i : allPages) {
            if (i.getSeite().getId().equals(x.getId())) {
                i.setDone(true);
                return true;
            }
        }
        return false;
    }
    
    public static class SeiteAndDone {
        private final SeiteSO seite;
        private boolean done = false;

        public SeiteAndDone(SeiteSO seite) {
            this.seite = seite;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }

        public SeiteSO getSeite() {
            return seite;
        }
    }
}
