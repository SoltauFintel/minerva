package minerva.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.base.MList;
import minerva.seite.Breadcrumb;
import minerva.seite.Seite;

public class SeitenSO extends MList<SeiteSO> {

	public SeitenSO(Seitensortierung parent) {
		super(getComparator(parent));
	}

	private static Comparator<SeiteSO> getComparator(Seitensortierung parent) {
		if (parent.isSorted()) {
			return (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle());
		} else {
			return (a, b) -> Integer.valueOf(a.getSeite().getPosition())
					.compareTo(Integer.valueOf(b.getSeite().getPosition()));
		}
	}

	public static SeitenSO findeUnterseiten(Seitensortierung parent, List<Seite> alleSeiten, BookSO book) {
		SeitenSO ret = new SeitenSO(parent);
		for (Seite seite : alleSeiten) {
			if (seite.getParentId().equals(parent.getId())) {
				ret.add(new SeiteSO(book, seite, alleSeiten));
			}
		}
		return ret;
	}

	/**
	 * @param id -
	 * @return Exception if Seite not found
	 */
	public SeiteSO byId(String id) {
		SeiteSO ret = _byId(id);
		if (ret == null) {
			Logger.error("Page not found: " + id);
			throw new RuntimeException("This page does not exist.");
		}
		return ret;
	}

	/**
	 * @param id -
	 * @return null if Seite not found
	 */
	public SeiteSO _byId(String id) {
		for (SeiteSO seite : this) {
			if (seite.getId().equals(id)) {
				return seite;
			}
			SeiteSO x = seite.getSeiten()._byId(id); // recursive
			if (x != null) {
				return x;
			}
		}
		return null;
	}

	public String createSeite(Seitensortierung parent, BookSO book) {
		return createSeite(parent, book, IdGenerator.createId6()).getId();
	}

	/**
	 * directly called for migration, otherwise use other createSeite() method
	 * @param parent -
	 * @param book -
	 * @param newId -
	 * @return SeiteSO
	 */
	public SeiteSO createSeite(Seitensortierung parent, BookSO book, String newId) {
		Seite neueSeite = new Seite();
		neueSeite.setId(newId);
		neueSeite.setParentId(parent.getId());
		neueSeite.setPosition(calculateNextPosition());
		MinervaWebapp.factory().setNewSeiteTitle(neueSeite.getTitle(), "" + neueSeite.getPosition());
		
		SeiteSO ret = new SeiteSO(book, neueSeite, parent);
		add(ret);
		return ret;
	}

	public int calculateNextPosition() {
		int max = 0;
		for (SeiteSO seite : this) {
			int position = seite.getSeite().getPosition();
			if (position > max) {
				max = position;
			}
		}
		return max + 1;
	}
	
	public void setPositionsAndSaveTo(Map<String, String> files) {
		int position = 1;
		for (SeiteSO sub : this) {
			sub.getSeite().setPosition(position++);
			sub.saveTo(files);
		}
	}

	public List<SeiteSO> searchInTitle(String search, String excludeSeiteId) {
		List<SeiteSO> ret = new ArrayList<>();
		for (SeiteSO seite : this) {
			if (!seite.getId().equals(excludeSeiteId) &&
					seite.getTitle().toLowerCase().contains(search.toLowerCase())) {
				ret.add(seite);
			}
			ret.addAll(seite.getSeiten().searchInTitle(search, excludeSeiteId));
		}
		return ret;
	}
	
	public boolean breadcrumbs(String seiteId, List<Breadcrumb> breadcrumbs) {
		for (SeiteSO seite : this) {
			if (seite.getId().equals(seiteId)) {
				return true; // gefunden, Ende der Suche
			}
			if (seite.getSeiten().breadcrumbs(seiteId, breadcrumbs)) {
				Breadcrumb b = new Breadcrumb();
				b.setTitle(seite.getSeite().getTitle());
				b.setLink("/s/" + seite.getBook().getWorkspace().getBranch() + "/"
						+ seite.getBook().getBook().getFolder() + "/" + seite.getId());
				breadcrumbs.add(b);
				return true;
			}
		}
		return false;
	}
	
	public void onlyRemove(SeiteSO seite) {
		remove(seite);
	}
}
