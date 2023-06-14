package minerva.model;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import github.soltaufintel.amalia.rest.REST;
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
    
    public SearchSO(String searchHost, WorkspaceSO workspace, List<String> langs) {
        this.host = searchHost;
        this.workspace = workspace;
        this.langs = langs;
    }

    public void indexBooks() {
        createSite();
        for (BookSO book : workspace.getBooks()) {
            index(book.getSeiten());
        }
    }
    
    private void createSite() {
        for (String lang : langs) {
            CreateSiteRequest req = new CreateSiteRequest();
            req.setLanguage(lang);
            post("/indexing/" + getSiteName(lang), req);
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
        try {
            for (String lang : langs) {
                String html = "<title>" + Escaper.esc(seite.getSeite().getTitle().getString(lang)) + "</title>"
                        + seite.getContent().getString(lang);
                String w = new String(html.getBytes(), "windows-1252");
                CreatePageRequest req = new CreatePageRequest();
                req.setHtml(w);
                req.setPath(seite.getBook().getBook().getFolder() + "/" + seite.getId());
                post("/indexing/" + getSiteName(lang) + "/page", req);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    private String getSiteName(String lang) {
        return "minerva-" + workspace.getBranch() + "-" + lang;
    }
    
    private void post(String url, Object data) {
        new REST(host + url).post(data).close();
    }
    
    public List<SearchResult> search(String x, String lang) {
        if (StringService.isNullOrEmpty(x)) {
            return new ArrayList<>();
        }
        String url = host + "/search/" + getSiteName(lang) + "?q=" + Escaper.urlEncode(x, "");
        Type type = new TypeToken<ArrayList<SearchResult>>() {}.getType();
        return new REST(url).get().fromJson(type);
    }
}
