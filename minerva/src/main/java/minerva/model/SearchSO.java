package minerva.model;

import static github.soltaufintel.amalia.web.action.Escaper.urlEncode;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.StringService;
import minerva.search.CreatePageRequest;
import minerva.search.CreateSiteRequest;
import minerva.search.SearchResult;

/**
 * Indexing and search function
 * <p>Search service: https://github.com/SoltauFintel/xsearch</p>
 */
public class SearchSO {
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
        workspace.getBooks().forEach(book -> book.getAlleSeiten().forEach(seite -> index(seite))); // Index pages including all subpages
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
     * Index single page excluding subpages
     * @param seite page
     */
    public void index(SeiteSO seite) {
        for (String lang : langs) {
            CreatePageRequest req = new CreatePageRequest();
            req.setHtml("<title>" + Escaper.esc(seite.getSeite().getTitle().getString(lang)) + "</title>"
                    + seite.getContent().getString(lang));
            req.setPath(getPath(seite));
            post("/indexing/" + getSiteName(lang) + "/page", req);
            nPages++;
        }
    }

    public void unindex(SeiteSO seite) {
        if (host != null) {
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
        if (host != null) {
            REST.post_cp1252(host + url, data);
        }
    }
    
    public List<SearchResult> search(String x, String lang) {
        if (StringService.isNullOrEmpty(x) || host == null) {
            return new ArrayList<>();
        }
        String url = host + "/search/" + getSiteName(lang) + "?q=" + Escaper.urlEncode(x, "");
        Type type = new TypeToken<ArrayList<SearchResult>>() {}.getType();
        return new REST(url).get().fromJson(type);
    }
}
