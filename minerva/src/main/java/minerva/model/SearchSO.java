package minerva.model;

import static github.soltaufintel.amalia.web.action.Escaper.urlEncode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
    public static List<WorkspaceSearcher> additionalSearchers = new ArrayList<>();
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
    
    public List<SearchResult> search(String x, boolean forceContainsSearch, String lang, boolean isFirstLanguage) {
        List<SearchResult> ret;
        if (StringService.isNullOrEmpty(x)) {
            return new ArrayList<>();
        } else if (!StringService.isNullOrEmpty(host)) {
        	// search pages using xsearch
            String url = host + "/search/" + getSiteName(lang) + "?q=" + Escaper.urlEncode(x, "") + (forceContainsSearch ? "&contains=force" : "");
            Type type = new TypeToken<ArrayList<SearchResult>>() {}.getType();
            Logger.debug(url);
            ret = new REST(url).get().fromJson(type);
			ret.forEach(sr -> {
				sr.setTitle(Escaper.esc(sr.getTitle()));
				sr.setIcon("fa-book greenbook");
			});
        } else {
        	ret = new ArrayList<>();
        }

        // search in all pages with many algorithms
        List<SeiteSearcher> searchers = getSearchers(workspace, isFirstLanguage);
        SearchContext sc = new SearchContext(x, lang, ret);
    	for (BookSO book : workspace.getBooks()) {
    		book.getAlleSeiten().forEach(seite ->
				searchers.forEach(searcher -> searcher.search(sc, seite))
			);
		}
        
        addBreadcrumbs(ret);
		
        // search in other data
		additionalSearchers.forEach(i -> i.search(sc, workspace));
    	
        boolean again = true;
        while (again) {
        	again = merge(ret);
        }
        
        return ret;
    }
    
    // page search algorithms
    private List<SeiteSearcher> getSearchers(WorkspaceSO workspace, boolean isFirstLanguage) {
    	List<SeiteSearcher> ret = new ArrayList<>();
    	
    	// search by Seite ID
    	if (isFirstLanguage) {
    		ret.add((sc, seite) -> {
				if (seite.getId().equalsIgnoreCase(sc.getX())) {
		            sc.add(seite, "ID: " + seite.getId()).setIcon("fa-key greenbook");
				}
			});
    	}
    	
    	// search in attachments
		ret.add((sc, seite) -> new AttachmentsSO(seite).search(sc));
		
		// search features
		ret.add(new FeatureFieldsService().getSearcher(workspace));
		
    	return ret;
    }

    private void addBreadcrumbs(List<SearchResult> result) {
    	for (SearchResult r : result) {
			if (r.getBreadcrumbs().isEmpty()) {
				try {
					String[] p = r.getPath().split("/"); // e.g. "handbuch/843xwy"
					String bookFolder = p[0];
					String id = p[1];
					BookSO book = workspace.getBooks().byFolder(bookFolder);
					r.setCategory(book.getBookFilterId());
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

    // Suchtreffer, die das gleiche Ziel haben, verschmelzen.
    private boolean merge(List<SearchResult> sr) {
		for (int i = 0; i < sr.size(); i++) {
			SearchResult ii = sr.get(i);
			String iPath = ii.getPath();
			for (int j = 0; j < i; j++) {
				SearchResult jj = sr.get(j);
				if (iPath.equals(jj.getPath())) {
					// i und j müssten vereinigt werden
					ii.merge(jj);
					sr.remove(j); // j muss weg
					return true; // von vorne beginnen
				}
			}
		}
		return false;
    }
    
    public static class SearchContext {
    	private final String x;
    	private final String lang;
    	private final List<SearchResult> result;
    	private final Pattern pattern;

    	public SearchContext(String x, String lang, List<SearchResult> result) {
			this.x = x;
			this.lang = lang;
			this.result = result;
			this.pattern = Pattern.compile(".*" + StringService.unquote(Pattern.quote(x)) + ".*", Pattern.CASE_INSENSITIVE);
		}

		public String getX() {
			return x;
		}
		
		public Pattern getPattern() {
			return pattern;
		}
		
		public boolean matches(String str) {
			return str != null && pattern.matcher(str).find();
		}

		public String getLang() {
			return lang;
		}
		
		public boolean isGerman() {
			return "de".equalsIgnoreCase(lang);
		}

		public SearchResult add(SeiteSO seite, String content) {
			SearchResult sr = new SearchResult();
			sr.setCategory(seite.getBook().getBookFilterId());
            sr.setPath(seite.getBook().getBook().getFolder() + "/" + seite.getId());
            if (seite.isFeatureTree()) {
            	sr.setTitle(Escaper.esc(seite.getSeite().getTitle().getString("de")));
            } else {
            	sr.setTitle(Escaper.esc(seite.getTitle()));
            }
            sr.setContent(content);
            add(sr);
            return sr;
		}
		
		public void add(SearchResult hit) {
			result.add(hit);
		}
    }

	public interface SeiteSearcher {

		void search(SearchContext sc, SeiteSO seite);
	}

	public interface WorkspaceSearcher {

		void search(SearchContext sc, WorkspaceSO workspace);
	}
}
