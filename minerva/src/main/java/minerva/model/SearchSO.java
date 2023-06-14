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
import minerva.search.SearchResult;
import minerva.search.indexing.CreatePageRequest;
import minerva.search.indexing.CreateSiteRequest;

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
        createSite();
        for (BookSO book : workspace.getBooks()) {
            index(book.getSeiten());
        }
        Logger.info("All books of workspace " + workspace.getBranch() + " have been indexed.");
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
     * Index pages including all subpages
     * @param seiten pages
     */
    private void index(SeitenSO seiten) {
        for (SeiteSO seite : seiten) {
            index(seite);
            index(seite.getSeiten()); // recursive
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
            req.setPath(seite.getBook().getBook().getFolder() + "/" + seite.getId());
            post("/indexing/" + getSiteName(lang) + "/page", req);
        }
    }
    
    private String getSiteName(String lang) {
        return sitePrefix + workspace.getBranch() + "-" + lang;
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
