package minerva.validate;

import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.model.SeiteSO;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

public class ValidationPage extends BPage {
    private int nMessages;
    private int nPages;

    @Override
    protected void execute() {
        nMessages = 0;
        nPages = 0;
        header(n("validate"));
        DataList hauptliste = list("hauptliste");
        for (String lang : langs) {
            DataMap langEintrag = hauptliste.add();
            langEintrag.put("lang", lang);
            DataList seiten = langEintrag.list("seiten");
            DataList links = langEintrag.list("links");
            for (SeiteSO seite : book.getAlleSeiten()) {
                validate(seite, lang, seiten);
                extractLinks(seite, lang, links);
            }
            langEintrag.put("hasEntries", !seiten.isEmpty());
            langEintrag.put("hasLinks", !links.isEmpty());
        }
        putInt("nPages", nPages);
        putInt("nMessages", nMessages);
    }

    private void validate(SeiteSO seite, String lang, DataList seiten) {
        List<String> msg = new ValidatorService().validate(seite, lang, user.getGuiLanguage());
        if (!msg.isEmpty()) {
            DataMap map = seiten.add();
            map.put("title", esc(seite.getSeite().getTitle().getString(lang)));
            map.put("link", "/s/" + esc(branch) + "/" + esc(book.getBook().getFolder()) + "/" + seite.getId());
            DataList list2 = map.list("fehlerliste");
            for (String text : msg) {
                DataMap map2 = list2.add();
                map2.put("text", text);
                nMessages++;
            }
            map.putSize("n", msg);
            nPages++;
        }
    }

    private void extractLinks(SeiteSO seite, String lang, DataList links) {
        String html = seite.getContent().getString(lang);
        List<Link> xlinks = LinkService.extractLinks(html, true);
        for (Link link : xlinks) {
            if (link.getHref().startsWith("http://") || link.getHref().startsWith("https://")) {
                DataMap map = links.add();
                map.put("href", link.getHref());
                map.put("title", link.getTitle());
                map.put("pagelink", "/s/" + seite.getBook().getWorkspace().getBranch() + "/" + seite.getBook().getBook().getFolder() + "/" +
                        seite.getId());
                map.put("pagetitle", esc(seite.getTitle()));
            }
        }
    }
}
