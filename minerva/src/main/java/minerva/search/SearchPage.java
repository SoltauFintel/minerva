package minerva.search;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.StringService;
import minerva.user.UPage;

public class SearchPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String q = ctx.queryParam("q");
        String lang = ctx.queryParam("lang");
        if (StringService.isNullOrEmpty(lang)) {
            lang = user.getLanguage();
        }

        if (isPOST()) {
            ctx.redirect("/b/" + branch + "/search?q=" + u(q) + "&lang=" + u(lang));
        } else {
            List<SearchResult> result = user.getWorkspace(branch).getSearch().search(q, lang);

            put("branch", esc(branch));
            put("q", esc(q));
            put("lang", esc(lang));
            DataList list = list("result");
            for (SearchResult s : result) {
                DataMap map = list.add();
                map.put("title", esc(s.getTitle()));
                map.put("path", esc("/s/" + branch + "/" + s.getPath()));
                map.put("content", s.getContent());
            }
            putInt("n", result.size());
            put("hasq", !StringService.isNullOrEmpty(q));
        }
    }
    
    private String u(String text) {
        return Escaper.urlEncode(text, "");
    }
}
