package minerva.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	private List<BookSO> booksfilter = new ArrayList<>();
	private BookSO qb_book;
	
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String q = ctx.queryParam("q");
        String qb = ctx.queryParam("qb");

        if (isPOST()) {
        	String params = StringService.isNullOrEmpty(qb) ? "" : "&qb=" + u(qb);
        	ctx.redirect("/w/" + esc(branch) + "/search?q=" + u(q) + params);
        } else {
        	n = 0;
        	WorkspaceSO workspace = user.getWorkspace(branch);
			qb_book = StringService.isNullOrEmpty(qb) ? null : workspace.getBooks()._byFolder(qb);
        	
            Map<String, List<SearchResult>> results = getResults(branch, q);

			header(n("volltextsuche") + ": " + q);
            put("branch", esc(branch));
            put("searchFocus", true);
            put("q", esc(q));
            put("hasq", !StringService.isNullOrEmpty(q));
			fillList(results, workspace);
            putInt("n", n);

            fillBooksfilter(q, workspace);
			Logger.info(user.getLogin() + " | " + branch + " | Search for \"" + q + "\": " + n + " page"
					+ (n == 1 ? "" : "s") + (qb_book != null ? " | qb: " + qb_book.getBook().getFolder() : ""));
        }
    }

	private Map<String, List<SearchResult>> getResults(String branch, String q) {
		Map<String, List<SearchResult>> results = new HashMap<>();
		boolean first = true;
		for (String lang : langs) { 
		    List<SearchResult> result = user.getWorkspace(branch).getSearch().search(q, lang, first);
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
		if (qb_book != null && book != null && !book.getBook().getFolder().equals(qb_book.getBook().getFolder())) {
			return;
		}
		n++;
		
		DataMap map = list.add();
		map.put("title", esc(s.getTitle()));
		map.put("path", esc(s.getPath()));
		map.put("content", s.getContent());
		boolean hasBreadcrumbs = !s.getBreadcrumbs().isEmpty();
		map.put("hasBreadcrumbs", hasBreadcrumbs);
		map.put("isFeatureTree", book == null ? false : book.isFeatureTree());
		map.put("isInternal", book == null ? false : book.isInternal());
		map.put("featureNumber", esc(s.getFeatureNumber()));
		map.putHas("featureNumber", s.getFeatureNumber());
		
		DataList list2 = map.list("breadcrumbs");
		for (int i = s.getBreadcrumbs().size() - 1; i >= 0; i--) {
			Breadcrumb b = s.getBreadcrumbs().get(i);
			list2.add() //
					.put("link", esc(b.getLink())) //
					.put("title", esc(b.getTitle().getString(e.getKey())));
		}
		
		if (book != null && !findBook(book)) {
			booksfilter.add(book);
			booksfilter.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));
		}
	}

	private BookSO getBook(SearchResult s, WorkspaceSO workspace) {
		if (!s.getBreadcrumbs().isEmpty()) {
			Breadcrumb first = s.getBreadcrumbs().get(s.getBreadcrumbs().size() - 1);
			String folder = first.getLink();
			int o = folder.lastIndexOf("/");
			folder = folder.substring(o + 1);
			return workspace.getBooks()._byFolder(folder);
		}
		return null;
	}
	
	private boolean findBook(BookSO book) {
		String folder = book.getBook().getFolder();
		return booksfilter.stream().anyMatch(i -> i.getBook().getFolder().equals(folder));
	}

	private void fillBooksfilter(String q, WorkspaceSO workspace) {
		put("hasBooksfilter", booksfilter.size() >= 2);
		DataList list = list("booksfilter");
		int i = 0;
		for (BookSO book : booksfilter) {
			list.add().put("title", esc(book.getTitle())) //
					.put("link", "search?q=" + u(q) + "&qb=" + u(book.getBook().getFolder())) //
					.put("last", i == booksfilter.size() - 1);
			i++;
		}
		put("hasqb", qb_book != null);
		put("qboff", "search?q=" + u(q));
	}
}
