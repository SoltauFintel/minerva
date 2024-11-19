package minerva.model;

import static github.soltaufintel.amalia.web.action.Escaper.urlEncode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import gitper.base.StringService;
import minerva.mask.FeatureFieldsService;
import minerva.search.CreatePageRequest;
import minerva.search.CreateSiteRequest;
import minerva.search.SearchResult;
import minerva.seite.ViewAreaBreadcrumbLinkBuilder;

/**
 * Indexing and search function
 * <p>Search service: https://github.com/SoltauFintel/xsearch</p>
 */
public class SearchSO {
    public static Searcher additionalSearcher = null;
    private final String host;
    private final WorkspaceSO workspace;
    private final List<String> langs;
    private final String sitePrefix;
    private int nPages;
    
    public SearchSO(String searchHost, String sitePrefix, WorkspaceSO workspace, List<String> langs) {
        this.host = searchHost;
        this.sitePrefix = sitePrefix;
        this.workspace = workspace;
        this.langs = langs;
    }

    public void indexBooks() {
        // If this takes too long the search service has to be restarted.
        long start = System.currentTimeMillis();
        nPages = 0;
        createSite();
        
		// Index pages including all subpages
		workspace.getBooks().forEach(book -> {
			for (String lang : langs) {
				if (!book.isFeatureTree() || "de".equals(lang)) {
					book.getAlleSeiten().forEach(seite -> index(seite, lang));
				}
			}
		});
        
        long end = System.currentTimeMillis();
        Logger.info("All books of workspace " + workspace.getBranch() + " have been reindexed. "
                + nPages + " pages, " + (end - start) + "ms");
    }
    
    private void createSite() {
        for (String lang : langs) {
            CreateSiteRequest req = new CreateSiteRequest();
            req.setLanguage(lang);
            String siteName = getSiteName(lang);
            Logger.info("creating site by calling " + host + "/indexing/" + siteName + " ...");
            post("/indexing/" + siteName, req);
            Logger.info("created site: " + siteName);
        }
    }
    
    /**
     * Index single page excluding subpages - all languages
     * @param seite page
     */
    public void index(SeiteSO seite) {
    	for (String lang : langs) {
			if (!seite.isFeatureTree() || "de".equals(lang)) {
				index(seite, lang);
    		}
    	}
    }
    
    /**
     * Index single page excluding subpages
     * @param seite page
     * @param lang one language
     */
    private void index(SeiteSO seite, String lang) {
        CreatePageRequest req = new CreatePageRequest();
        req.setHtml("<title>" + Escaper.esc(seite.getSeite().getTitle().getString(lang)) + "</title>"
                + seite.getContent().getString(lang));
        req.setPath(getPath(seite));
        post("/indexing/" + getSiteName(lang) + "/page", req);
        nPages++;
    }

    public void unindex(SeiteSO seite) {
        if (!StringService.isNullOrEmpty(host)) {
            for (String lang : langs) {
                String path = "/indexing/" + getSiteName(lang) + "/page?path=" + urlEncode(getPath(seite), "");
                REST.delete(host + path);
            }
        }
    }

    private String getSiteName(String lang) {
        return sitePrefix + workspace.getBranch() + "-" + lang;
    }
    
    private String getPath(SeiteSO seite) {
        return seite.getBook().getBook().getFolder() + "/" + seite.getId();
    }
    
    private void post(String url, Object data) {
        if (!StringService.isNullOrEmpty(host)) {
            REST.post_cp1252(host + url, data);
        }
    }
    
    public List<SearchResult> search(String x, String lang, boolean isFirstLanguage) {
        List<SearchResult> ret;
        if (StringService.isNullOrEmpty(x)) {
            return new ArrayList<>();
        } else if (!StringService.isNullOrEmpty(host)) {
        	// search pages using xsearch
            String url = host + "/search/" + getSiteName(lang) + "?q=" + Escaper.urlEncode(x, "");
            Type type = new TypeToken<ArrayList<SearchResult>>() {}.getType();
            Logger.debug(url);
            ret = new REST(url).get().fromJson(type);
        } else {
        	ret = new ArrayList<>();
        }
        
        if (isFirstLanguage) {
            searchBySeiteID(x, ret);
        }
        
        // search features
        new FeatureFieldsService().search(workspace, x.toLowerCase(), lang, ret);
        addBreadcrumbs(ret);
		
        // search in other data
        if (additionalSearcher != null) {
			List<SearchResult> ret2 = additionalSearcher.search(workspace, x.toLowerCase(), lang);
			if (ret2 != null) {
				ret.addAll(ret2);
			}
        }
        return ret;
    }

    private void searchBySeiteID(String x, List<SearchResult> result) {
        SeiteSO seite = workspace.findPage(x);
        if (seite != null) {
            SearchResult sr = new SearchResult();
            sr.setPath(seite.getBook().getBook().getFolder() + "/" + seite.getId());
            sr.setTitle(seite.getTitle());
            sr.setContent("ID: " + seite.getId());
            result.add(sr);
        }
    }

    private void addBreadcrumbs(List<SearchResult> result) {
    	for (SearchResult r : result) {
			if (r.getBreadcrumbs().isEmpty()) {
				try {
					String[] p = r.getPath().split("/"); // e.g. "handbuch/843xwy"
					String bookFolder = p[0];
					String id = p[1];
					BookSO book = workspace.getBooks().byFolder(bookFolder);
					r.getBreadcrumbs().addAll(book.getBreadcrumbs(id, new ViewAreaBreadcrumbLinkBuilder()));
					if (book.isFeatureTree() && r.getFeatureNumber() == null) {
						// Die normale MongoDB-basierte Suche hat ein Feature gefunden. Hier muss dann noch die Feature-Nummer gesetzt werden.
						r.setFeatureNumber(new FeatureFieldsService().get(book._seiteById(id)).getFeatureNumber());
					}
				} catch (Exception e) {
					Logger.debug("path: " + r.getPath());
					Logger.debug(e);
				}
			}
		}
	}

	public interface Searcher {
    	List<SearchResult> search(WorkspaceSO workspace, String x, String lang);
    }
}
