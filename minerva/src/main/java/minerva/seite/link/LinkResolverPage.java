package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class LinkResolverPage extends SPage {
    private String pre;
    private List<SeiteSO> result;

    @Override
    protected void execute() {
        String key = ctx.queryParam("key");
        int index = Integer.valueOf(ctx.queryParam("index"));

        if (isPOST()) {
            search(ctx.formParam("search"), key, index);
        } else {
            showPage(key, index);
        }
    }

    private void showPage(String key, int index) {
        LinksModel linksModel = LinksModel.get(key);

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
        String href = link == null ? "" : esc(link.getHref());
        pre = "/links/" + branch + "/" + bookFolder + "/" + id + "?key=" + key + "&index=" + (index + 1);
        result = new ArrayList<>();
        search(href, key, index);

        fill(key, index, linksModel, link, href);
    }

    private void fill(String key, int index, LinksModel linksModel, Link link, String href) {
        put("href", href);
        put("aTitle", link == null ? "" : esc(link.getTitle()));
        putInt("nextIndex", index + 1);
        putInt("index", index);
        putInt("size", linksModel.getLinks().size());
        put("key", Escaper.urlEncode(key, ""));
        put("result0", makeSearchHTML());
    }

    private void search(String search, String key, int index) {
        if (search.trim().length() <= 1) {
            result = new ArrayList<>();
        } else {
            result = book.getSeiten().searchInTitle(search.toLowerCase(), id, langs);
            Logger.info("search: \"" + search + "\", found: " + result.size());
        }
        pre = "/links/" + branch + "/" + bookFolder + "/" + id + "?key=" + key + "&index=" + (index + 1);
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
            String link = pre + "&s=" + Escaper.urlEncode(seite.getId(), "");
            html += addResult(link, n("linkResolver1").replace("$t", esc(seite.getTitle())), nr++);
        }
        html += addResult(pre, "<i class=\"fa fa-ban\"></i> " + n("linkResolver0"), nr);
        return html;
    }

    private String addResult(String link, String title, int nr) {
        return "<tr><td><a href=\"" + link + "\" onclick=\"document.querySelector('#s" + nr + "').style='';\">"
                + title + " <i id=\"s" + nr
                + "\" class=\"fa fa-delicious fa-spin\" style=\"display: none;\"></i></a></td></tr>";
    }
}
