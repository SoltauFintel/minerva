package minerva.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.rest.RestResponse;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.StringService;
import minerva.search.CreatePageRequest;
import minerva.search.CreateSiteRequest;
import minerva.search.SearchResult;

/**
 * Indexing and search function
 */
public class SearchSO {
    private final String host;
    private final WorkspaceSO workspace;
    private final List<String> langs;
    private final String sitePrefix;
    
    public SearchSO(String searchHost, String sitePrefix, WorkspaceSO workspace, List<String> langs) {
        this.host = searchHost;
        this.sitePrefix = sitePrefix;
        this.workspace = workspace;
        this.langs = langs;
    }

    public void indexBooks() {
        long start = System.currentTimeMillis();
        createSite();
        workspace.getBooks().forEach(book -> book.getAlleSeiten().forEach(seite -> index(seite))); // Index pages including all subpages
        long end = System.currentTimeMillis();
        Logger.info("All books of workspace " + workspace.getBranch() + " have been reindexed. "
                + (end - start) + "ms");
    }
    
    private void createSite() {
        for (String lang : langs) {
            CreateSiteRequest req = new CreateSiteRequest();
            req.setLanguage(lang);
            String siteName = getSiteName(lang);
            Logger.info("create site: " + siteName);
            post("/indexing/" + siteName, req);
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
        }
    }

    public void unindex(SeiteSO seite) {
        if (host != null) {
            for (String lang : langs) {
                String url = "/indexing/" + getSiteName(lang) + "/page?path=" + Escaper.urlEncode(getPath(seite), "");
                new REST(host + url).delete().close();
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
            new REST(host + url) {
                @Override
                protected RestResponse request(HttpEntityEnclosingRequestBase request, Object data) {
                    try {
                        return request(request, new Gson().toJson(data), "application/json; charset=cp1252");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            .post(data)
            .close();
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
