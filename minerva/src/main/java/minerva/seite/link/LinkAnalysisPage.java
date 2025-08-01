package minerva.seite.link;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.table.Col;
import github.soltaufintel.amalia.web.table.TableComponent;
import minerva.book.BookPage;
import minerva.seite.SPage;

public class LinkAnalysisPage extends SPage {

    @Override
    protected void execute() {
        List<LinkAnalysisEntry> result = new LinkAnalysisService(branch, bookFolder, seite, langs).get();
        
        header(n("linkAnalysis"));
        DataList list = list("links");
        for (LinkAnalysisEntry e : result) {
            DataMap map = list.add();
            map.put("href", esc(e.getHref()));
            map.put("linkTitle", esc(e.getLinkTitle())); // This is the link title.
            map.put("pageTitle", esc(e.getPageTitle()));
            map.put("lang", esc(e.getLang()));
            map.put("internal", e.isInternal());
            map.put("outgoing", e.isOutgoing());
            map.put("crossbook", e.isCrossBook());
            
            map.put("c2", (e.isOutgoing() ? "o" : "i") + (e.isCrossBook() ? "c" : (e.isInternal() ? "i" : "e")));
            map.put("c3", e.isCrossBook() ? "" : e.getLinkTitle());
            map.put("c4", e.getPageTitle());
        }
        List<Col> cols = List.of(
        		new Col(n("language"), "{{i.lang}}").sortable("lang"),
        		new Col(n("type"),
        		"""
		        {{if i.outgoing}}<span style="color: #0a0;">outgoing</span>{{else}}<span style="color: blue;">incoming</span>{{/if}}
		        {{if i.crossbook}}<span style="color: #909;">cross-book</span>{{else if i.internal}}internal{{else}}<span style="color: #c00;">external</span>{{/if}}
        		""").sortable("c2"),
        		new Col(n("linkTitle"), "{{if not i.crossbook}}<a href=\"{{i.href}}\">{{i.linkTitle}}</a>{{/if}}")
        			.sortable("c3"),
        		new Col(n("pageTitle"), "{{if i.crossbook}}<a href=\"{{i.href}}\">{{i.pageTitle}}</a>{{else}}{{i.pageTitle}}{{/if}}")
        			.sortable("c4"));
        put("table1", new TableComponent("wauto", cols, model, "links"));
        put("hasLinks", !result.isEmpty());

        if (book.isNotPublic()) {
            BookPage.oneLang(model, book);
        }
    }
}
