package minerva.mask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.template72.data.DataList;
import com.google.gson.Gson;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.access.DirAccess;
import gitper.access.MultiPurposeDirAccess;
import gitper.base.StringService;
import minerva.mask.FeatureFields.MaskLabelContext;
import minerva.model.BookSO;
import minerva.model.SearchSO.ISearcher;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.seite.ViewSeitePage.AddFeatures;

public class FeatureFieldsService implements AddFeatures {

    public FeatureFields get(SeiteSO seite) {
        FeatureFields ff = load(seite);
        return ff == null ? FeatureFields.create(seite) : ff;
    }

    private FeatureFields load(SeiteSO seite) {
        return new MultiPurposeDirAccess(seite.getBook().dao()).load(dn(seite), FeatureFields.class);
    }
    
    public void set(SeiteSO seite, FeatureFields featureFields) {
        if (StringService.isNullOrEmpty(featureFields.getSeiteId())
                || StringService.isNullOrEmpty(featureFields.getMaskTag())) {
            throw new IllegalArgumentException("seiteId and/or maskTag must not be empty");
        }
        BookSO book = seite.getBook();
        new MultiPurposeDirAccess(book.dao()).save(dn(seite), featureFields, seite.commitMessage("feature fields"), book.getWorkspace());
    }
    
    public void removeEntryAndSave(FeatureFields ff, String id, SeiteSO seite) {
        ff.getPages().remove(id);
        set(seite, ff);
    }
    
    public void delete(SeiteSO seite) {
        BookSO book = seite.getBook();
        if (!new MultiPurposeDirAccess(book.dao()).delete(dn(seite), seite.commitMessage("feature fields deleted"),
                book.getWorkspace())) {
            throw new RuntimeException("Error deleting feature fileds for page " + seite.getId());
        }
    }
    
    public static String dn(SeiteSO seite) {
        return seite.getBook().getFolder() + "/feature-fields/" + seite.getId() + ".ff";
    }
    
    public boolean findValue(SeiteSO excludeSeite, String id, String value) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        DirAccess dao = excludeSeite.getBook().dao();
        Map<String, String> files = dao.loadAllFiles(excludeSeite.getBook().getFolder() + "/feature-fields", ".ff");
        Gson gson = new Gson();
        for (Entry<String, String> e : files.entrySet()) {
            if (!e.getKey().endsWith("/feature-fields/" + excludeSeite.getId() + ".ff")) {
                FeatureFields ff = gson.fromJson(e.getValue(), FeatureFields.class);
                String cv = ff.get(id);
                if (cv != null && cv.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Listet auf welche Mitarbeiter für welche Features verantwortlich sind
     * @param book -
     * @return -
     */
    public List<Responsible> responsibles(BookSO book) {
        if (!book.isFeatureTree()) {
            throw new RuntimeException("Only for feature tree");
        }
        List<Responsible> ret = new ArrayList<>();
        for (SeiteSO seite : book.getAlleSeiten()) {
            FeatureFields dataFields = load(seite);
            if (dataFields != null) {
                String key = dataFields.get("responsible");
                if (!StringService.isNullOrEmpty(key)) {
                    find(key, ret).add(seite);
                }
            }
        }
        String currentUser = book.getUserRealName();
        ret.sort((a, b) -> {
        	String acu = a.name.equals(currentUser) ? "1" : "2";
        	String bcu = b.name.equals(currentUser) ? "1" : "2";
        	int r = acu.compareTo(bcu);
        	if (r == 0) {
	            r = b.seiten.size() - a.seiten.size();
	            return r == 0 ? a.name.compareToIgnoreCase(b.name) : r;
        	}
        	return r;
        });
        return ret;
    }

    private Responsible find(String name, List<Responsible> list) {
        for (Responsible r : list) {
            if (r.name.equalsIgnoreCase(name)) {
                return r;
            }
        }
        Responsible r = new Responsible();
        r.name = name;
        list.add(r);
        return r;
    }
    
    public static class Responsible {
        /** user name */
        public String name;
        public final List<RSeite> seiten = new ArrayList<>();
        
        public void add(SeiteSO seite) {
            RSeite rs = new RSeite();
            rs.seiteId = seite.getId();
            rs.title = seite.getTitle();
            rs.featureNumber = new FeatureFieldsService().get(seite).getFeatureNumber();
            seiten.add(rs);
            seiten.sort((a, b) -> a.title.compareToIgnoreCase(b.title));
        }
    }
    
    /**
     * Responsible: seiteId + title pair
     */
    public static class RSeite {
    	/** can also be a link */
        public String seiteId;
        public String title;
        public String featureNumber;
    }

    public ISearcher getSearcher(WorkspaceSO workspace) {
    	MaskLabelContext mlcontext = new MaskLabelContext(workspace);
    	return (sc, seite) -> {
            if (!"de".equals(sc.getLang()) || sc.getX() == null || sc.getX().isBlank() || !seite.isFeatureTree()) {
                return;
            }
        	FeatureFields dataFields = load(seite);
    		if (dataFields == null) {
    			return;
    		}
    		String lv = dataFields.search(sc.getX().toLowerCase(), mlcontext);
    		if (lv != null) {
				sc.add(seite, lv).setFeatureNumber(dataFields.getFeatureNumber());
    		}
    	};
    }

	@Override
	public void addFeatures(SeiteSO seite, DataList features) {
        new FeatureFieldsService()
        	.getFeaturesForSeite(seite.getId(), seite.getBook().getWorkspace())
        	.forEach(f -> features.add() //
        			.put("link", Escaper.esc(f.seiteId)) //
        			.put("title", Escaper.esc(f.title)) //
        			.put("sch", false) //
        			.put("featurenumber", Escaper.esc(f.featureNumber)) //
        			.putHas("featurenumber", f.featureNumber));
	}

	private List<RSeite> getFeaturesForSeite(String seiteId, WorkspaceSO workspace) {
        List<RSeite> features = new ArrayList<>();
        FeatureFieldsService sv = new FeatureFieldsService();
        for (BookSO book : workspace.getBooks()) {
            if (book.isFeatureTree()) {
                for (SeiteSO seite : book.getAlleSeiten()) {
                    FeatureFields ff = sv.get(seite);
                    if (ff.getPages().contains(seiteId)) {
                        RSeite feature = new RSeite();
                        feature.seiteId = "../" + seite.getBook().getBook().getFolder() + "/" + seite.getId();
                        feature.title = seite.getSeite().getTitle().getString("de");
                        feature.featureNumber = ff.getFeatureNumber();
                        features.add(feature);
                    }
                }
            }
        }
        return features;
    }
	
	public SeiteSO byFeatureNumber(BookSO book, String nr) {
		for (SeiteSO seite : book.getAlleSeiten()) {
			String fnr = get(seite).getFeatureNumber();
			if (fnr != null && nr.equalsIgnoreCase(fnr)) {
				return seite;
			}
		}
		throw new RuntimeException("Feature number '" + nr + "' not found in book '" + book.getTitle() + "'!");
	}
}
