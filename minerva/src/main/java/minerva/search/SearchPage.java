package minerva.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Breadcrumb;
import minerva.user.UPage;

public class SearchPage extends UPage {
	private int n;
	
    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String q = ctx.queryParam("q");

        if (isPOST()) {
            ctx.redirect("/w/" + esc(branch) + "/search?q=" + u(q));
        } else {
        	n = 0;
            Map<String, List<SearchResult>> results = getResults(branch, q);
            Logger.info(user.getLogin() + " | " + branch
                    + " | Search for \"" + q + "\": " + n + " page" + (n == 1 ? "" : "s"));

            put("branch", esc(branch));
            put("searchFocus", true);
            put("q", esc(q));
            put("hasq", !StringService.isNullOrEmpty(q));
            fillList(results, user.getWorkspace(branch));
            putInt("n", n);
        }
    }

	private Map<String, List<SearchResult>> getResults(String branch, String q) {
		Map<String, List<SearchResult>> results = new HashMap<>();
		for (String lang : langs) { 
		    List<SearchResult> result = user.getWorkspace(branch).getSearch().search(q, lang);
		    int nn = result.size();
		    if (nn > 0) {
		        n += nn;
		        results.put(lang, result);
		    }
		}
		return results;
	}

	private void fillList(Map<String, List<SearchResult>> results, WorkspaceSO workspace) {
		DataList list = list("langs");
		results.entrySet().stream()
		    .sorted((a, b) -> Integer.compare(b.getValue().size(), a.getValue().size()))
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
		DataMap map = list.add();
		map.put("title", esc(s.getTitle()));
		map.put("path", esc(s.getPath()));
		map.put("content", s.getContent());
		
		DataList list2 = map.list("breadcrumbs");
		for (int i = s.getBreadcrumbs().size() - 1; i >= 0; i--) {
			Breadcrumb b = s.getBreadcrumbs().get(i);
			list2.add() //
					.put("link", esc(b.getLink())) //
					.put("title", esc(b.getTitle().getString(e.getKey())));
		}
		boolean hasBreadcrumbs = !s.getBreadcrumbs().isEmpty();
		map.put("hasBreadcrumbs", hasBreadcrumbs);
		map.put("isFeatureTree", false);
		map.put("isInternal", false);
		if (hasBreadcrumbs) {
			try {
				String a = s.getBreadcrumbs().get(s.getBreadcrumbs().size() - 1).getLink();
				int o = a.lastIndexOf("/");
				a = a.substring(o + 1);
				BookSO book = workspace.getBooks().byFolder(a);
				map.put("isFeatureTree", book.isFeatureTree());
				map.put("isInternal", book.isInternal());
			} catch (Exception ignore) {
			}
		}
	}
}
