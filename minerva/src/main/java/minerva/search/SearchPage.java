package minerva.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import gitper.base.StringService;
import minerva.model.BookSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Breadcrumb;
import minerva.user.UPage;

public class SearchPage extends UPage {
    private int n;
    private Set<BookFilter> booksfilter = new TreeSet<>();
    private String qb;
    
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String q = ctx.queryParam("q");
        q = q == null ? "" : q;
        qb = ctx.queryParam("qb");
        boolean forceContainsSearch = "force".equals(ctx.queryParam("contains"));

        if (isPOST()) {
            String params = StringService.isNullOrEmpty(qb) ? "" : "&qb=" + u(qb);
            ctx.redirect("/w/" + esc(branch) + "/search?q=" + u(q) + params);
        } else {
            n = 0;
            WorkspaceSO workspace = user.getWorkspace(branch);
            long start = System.currentTimeMillis();
            
            Map<String, List<SearchResult>> results = getResults(branch, q, forceContainsSearch);

            start = System.currentTimeMillis() - start;
            header(n("volltextsuche") + ": " + q);
            put("branch", esc(branch));
            put("searchFocus", true);
            put("q", esc(q));
            put("hasq", !StringService.isNullOrEmpty(q));
            put("forceContainsSearch", forceContainsSearch);
            put("containsSearchLink", "/w/" + branch + "/search?q=" + u(q)
                    + (StringService.isNullOrEmpty(qb) ? "" : "&qb=" + u(qb)) + "&contains=force");
            fillList(results, workspace);
            putInt("n", n);
            put("quickbuttonsExtra", "&q=" + u(q));

            fillBooksfilter(q, workspace);
            Logger.info(user.getLogin() + " | " + branch + " | Search for \"" + q + "\": " + n + " page"
                    + (n == 1 ? "" : "s") + (qb == null ? "" : (" | qb: " + qb))
                    + (forceContainsSearch ? " | force contains search" : "") + " | " + start + "ms");
        }
    }

    private Map<String, List<SearchResult>> getResults(String branch, String q, boolean forceContainsSearch) {
        Map<String, List<SearchResult>> results = new HashMap<>();
        boolean first = true;
        for (String lang : langs) { 
            List<SearchResult> result = user.getWorkspace(branch).getSearch().search(q, forceContainsSearch, lang, first);
            first = false;
            int nn = result.size();
            if (nn > 0) {
                results.put(lang, result);
            }
        }
        return results;
    }

    private void fillList(Map<String, List<SearchResult>> results, WorkspaceSO workspace) {
        DataList list = list("langs");
        results.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size())) // Language with more hits at begin
            .forEach(e -> {
                DataMap map = list.add();
                map.put("lang", esc(e.getKey()));
                DataList list2 = map.list("result");
                for (SearchResult s : e.getValue()) {
                    fillResult(s, e, list2, workspace);
                }
            });
    }

    private void fillResult(SearchResult s, Entry<String, List<SearchResult>> e, DataList list, WorkspaceSO workspace) {
        BookSO book = getBook(s, workspace);
        
        // booksfilter Auswahl anwenden
        if (qb != null && !qb.equals(s.getCategory())) {
            return;
        }
        n++;
        
        DataMap map = list.add();
        map.put("title", s.getTitle()); // no esc
        map.put("path", esc(s.getPath()));
        map.put("content", s.getContent()); // no esc
        boolean hasBreadcrumbs = !s.getBreadcrumbs().isEmpty();
        map.put("hasBreadcrumbs", hasBreadcrumbs);
        map.put("isFeatureTree", book == null ? false : book.isFeatureTree());
        map.put("isInternal", book == null ? false : book.isInternal());
        map.put("featureNumber", esc(s.getFeatureNumber()));
        map.putHas("featureNumber", s.getFeatureNumber());
        map.put("isBook", s.getCategory().startsWith(BookSO.BOOK_PREFIX));
        map.put("icon", esc(s.getIcon()));
        
        DataList list2 = map.list("breadcrumbs");
        for (int i = s.getBreadcrumbs().size() - 1; i >= 0; i--) {
            Breadcrumb b = s.getBreadcrumbs().get(i);
            list2.add() //
                    .put("link", esc(b.getLink())) //
                    .put("title", esc(b.getTitle().getString(e.getKey())));
        }
        
        if (book != null && !findBook(book)) {
            booksfilter.add(book);
        } else if (s.getCategory() != null) {
            booksfilter.add(new BookFilter() {
                @Override
                public String getTitle() {
                    return s.getCategory();
                }

                @Override
                public String getBookFilterId() {
                    return s.getCategory();
                }
            });
        }
    }

    private BookSO getBook(SearchResult s, WorkspaceSO workspace) {
        return s.getCategory() == null || !s.getCategory().startsWith(BookSO.BOOK_PREFIX) ? null :
            workspace.getBooks()._byFolder(s.getCategory().substring(BookSO.BOOK_PREFIX.length()));
    }
    
    private boolean findBook(BookSO book) {
        String folder = book.getBook().getFolder();
        return booksfilter.stream().anyMatch(i -> i.getBookFilterId().equals(folder));
    }

    private void fillBooksfilter(String q, WorkspaceSO workspace) {
        put("hasBooksfilter", booksfilter.size() >= 1);
        DataList list = list("booksfilter");
        int i = 0;
        for (BookFilter book : booksfilter) {
            list.add().put("title", esc(book.getTitle())) //
                    .put("link", "search?q=" + u(q) + "&qb=" + u(book.getBookFilterId())) //
                    .put("last", i == booksfilter.size() - 1) //
                    .put("active", qb != null && qb.equals(book.getBookFilterId()));
            i++;
        }
        put("hasqb", qb != null);
        put("qboff", "search?q=" + u(q));
    }
}
