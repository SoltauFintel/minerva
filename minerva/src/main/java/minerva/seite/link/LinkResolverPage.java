package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class LinkResolverPage extends SPage {
    private String linkPrefix;
    private List<SeiteSO> result;

    @Override
    protected void execute() {
        int index = Integer.valueOf(ctx.queryParam("index"));

        if (isPOST()) {
            search(ctx.formParam("search"), index);
        } else {
            showPage(index);
        }
    }

    private void showPage(int index) {
        InvalidLinksModel linksModel = user.getLinksModel();

        if (linksModel == null || index < 0) {
            throw new RuntimeException("Error while resolving links: unknown key or index");
        }

        if (index > 0) {
            Link prevLink = linksModel.getLinks().get(index - 1);
            String s;
            if (ctx.req.queryParams().contains("s")) {
                s = ctx.queryParam("s"); // vorige Auswahl speichern
            } else {
                s = prevLink.getHref(); // unverändert lassen
            }
            prevLink.setSeiteId(s);
        }
        
        Link link = null;
        if (index >= linksModel.getLinks().size()) {
            linksModel.save(seite, langs);
            ctx.redirect(viewlink);
        } else {
            link = linksModel.getLinks().get(index);
        }
        String href = link == null ? "" : link.getHref();
        linkPrefix = "/links/" + branch + "/" + bookFolder + "/" + id + "?index=" + (index + 1);
        result = new ArrayList<>();
        search(href, index);

        fill(index, linksModel, link, href);
    }

    private void fill(int index, InvalidLinksModel linksModel, Link link, String href) {
        put("href", href);
        putInt("nextIndex", index + 1);
        putInt("index", index);
        putSize("size", linksModel.getLinks());
        put("result0", makeSearchHTML());
        put("linkResolverPage5", n("linkResolverPage5")
        		.replace("$h", href)
        		.replace("$p", seite == null ? "" : esc(seite.getTitle()))
        		.replace("$t", link == null ? "" : esc(link.getTitle())));
    }

    private void search(String search, int index) {
        if (search.trim().length() <= 1) {
            result = new ArrayList<>();
        } else {
            result = book.getSeiten().searchInTitle(StringEscapeUtils.unescapeHtml(search.toLowerCase()), id, langs);
            Logger.info("[LinkResolverPage] search: \"" + search + "\", found: " + result.size());
        }
        linkPrefix = "/links/" + branch + "/" + bookFolder + "/" + id + "?index=" + (index + 1);
    }

    @Override
    protected String render() {
        if (isPOST()) {
            return makeSearchHTML();
        } else {
            return templates.render(getPage(), model);
        }
    }

    private String makeSearchHTML() {
        String html = "";
        int nr = 1;
        for (SeiteSO seite : result) {
            String link = linkPrefix + "&s=" + Escaper.urlEncode(seite.getId(), "");
            html += addResult(link, n("linkResolver1").replace("$t", esc(seite.getTitle())), nr++);
        }
        html += addResult(linkPrefix, "<i class=\"fa fa-ban\"></i> " + n("linkResolver0"), nr);
        return html;
    }

    private String addResult(String link, String title, int nr) {
        return "<tr><td><a class=\"movelink\" href=\"" + link + "\""
                + " onclick=\"document.querySelector('#s" + nr + "').style='';\">"
                + title + " <i id=\"s" + nr
                + "\" class=\"fa fa-delicious fa-spin\" style=\"display: none;\"></i></a></td></tr>";
    }
}
