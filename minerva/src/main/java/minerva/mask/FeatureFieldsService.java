package minerva.mask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.google.gson.Gson;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.search.SearchResult;
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
        new MultiPurposeDirAccess(book.dao()).save(dn(seite), featureFields, new CommitMessage(seite, "feature fields"), book.getWorkspace());
    }
    
    public void removeEntryAndSave(FeatureFields ff, String id, SeiteSO seite) {
        ff.getPages().remove(id);
        set(seite, ff);
    }
    
    public void delete(SeiteSO seite) {
        BookSO book = seite.getBook();
        if (!new MultiPurposeDirAccess(book.dao()).delete(dn(seite), new CommitMessage(seite, "feature fields deleted"),
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
        long start = System.currentTimeMillis();
        Map<String, String> files = dao.loadAllFiles(excludeSeite.getBook().getFolder() + "/feature-fields", ".ff");
        Gson gson = new Gson();
        boolean ret = false;
        for (Entry<String, String> e : files.entrySet()) {
            if (!e.getKey().endsWith("/feature-fields/" + excludeSeite.getId() + ".ff")) {
                FeatureFields ff = gson.fromJson(e.getValue(), FeatureFields.class);
                String cv = ff.get(id);
                if (cv != null && cv.equals(value)) {
                    ret = true;
                    break;
                }
            }
        }
        long end = System.currentTimeMillis();
        Logger.info("find value \"" + value + "\" in field " + id + ": " + (ret ? "found" : "not found") + " | " + (end - start) + "ms");
        return ret;
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

    public void search(WorkspaceSO workspace, String q, String lang, List<SearchResult> result) {
        if (!"de".equals(lang) || q == null || q.isBlank()) {
            return;
        }
        final String x = q.toLowerCase();
        for (BookSO book : workspace.getBooks()) {
            if (!book.isFeatureTree()) {
                continue;
            }
            int n = 0;
            for (SeiteSO seite : book.getAlleSeiten()) {
                String path = seite.getBook().getBook().getFolder() + "/" + seite.getId();
                if (!exist(path, result)) {
                    FeatureFields dataFields = load(seite);
                    if (dataFields == null) {
                        continue;
                    }
                    String lv = dataFields.search(x, workspace);
                    if (lv != null) {
                        SearchResult sr = new SearchResult();
                        sr.setTitle(seite.getTitle());
                        sr.setPath(path);
                        sr.setContent(lv);
                        result.add(sr);
                        n++;
                    }
                }
            }
            if (n > 0) {
                Logger.info("Search \"" + q + "\" in " + book.getTitle() + " fields: " + n + " hit" + (n == 1 ? "" : "s"));
            }
        }
    }

    private boolean exist(String path, List<SearchResult> result) {
        for (SearchResult sr : result) {
            if (sr.getPath().equals(path)) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void addFeatures(SeiteSO seite, DataList features) {
        new FeatureFieldsService()
        	.getFeaturesForSeite(seite.getId(), seite.getBook().getWorkspace())
        	.forEach(f -> features.add() //
        			.put("link", Escaper.esc(f.seiteId)) //
        			.put("title", Escaper.esc(f.title)) //
        			.put("featurenumber", Escaper.esc(f.featureNumber))
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
