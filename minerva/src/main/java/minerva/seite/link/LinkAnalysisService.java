package minerva.seite.link;

import java.util.ArrayList;
import java.util.List;

import minerva.model.BookSO;
import minerva.model.BooksSO;
import minerva.model.SeiteSO;

public class LinkAnalysisService {
    private final List<LinkAnalysisEntry> result = new ArrayList<>();
    private final String branch;
    private final SeiteSO seite;
    private final List<String> langs;
    private final String linkPrefix;

    public LinkAnalysisService(String branch, String bookFolder, SeiteSO seite, List<String> langs) {
        this.branch = branch;
        this.seite = seite;
        this.langs = langs;
        linkPrefix = "/s/" + branch + "/" + bookFolder + "/";
    }
    
    public List<LinkAnalysisEntry> get() {
        result.clear();
        outgoingLinks();
        incomingLinks();
        outgoingCrossBookLinks();
        incomingCrossBookLinks();
        return result;
    }

    private void outgoingLinks() {
        for (String lang : langs) {
            List<Link> links = LinkService.extractLinks(seite.getContent().getString(lang), true);
            for (Link link : links) {
                boolean external = link.getHref().startsWith("http://") || link.getHref().startsWith("https://");
                LinkAnalysisEntry e = new LinkAnalysisEntry();
                e.setHref(external ? link.getHref() : linkPrefix + link.getHref());
                e.setId(link.getSeiteId());
                e.setLinkTitle(link.getTitle());
                e.setPageTitle(getPageTitle(link, external, lang));
                e.setLang(lang.toUpperCase());
                e.setInternal(!external);
                e.setOutgoing(true);
                e.setCrossBook(false);
                result.add(e);
            }
        }
    }
  
    private String getPageTitle(Link link, boolean external, String lang) {
        if (external) {
            return "";
        }
        SeiteSO s = seite.getBook()._seiteById(link.getHref());
        return s == null ? link.getTitle() : s.getSeite().getTitle().getString(lang);
    }
    
    private void incomingLinks() {
        for (SeiteSO s : seite.getBook().getAlleSeiten()) {
            for (String lang : langs) {
                List<Link> links = LinkService.extractLinks(s.getContent().getString(lang), true);
                for (Link link : links) {
                    if (link.getHref().equals(seite.getId())) {
                        LinkAnalysisEntry e = new LinkAnalysisEntry();
                        e.setId(s.getId());
                        e.setHref(linkPrefix + s.getId());
                        e.setLinkTitle(link.getTitle());
                        e.setPageTitle(s.getTitle());
                        e.setLang(lang.toUpperCase());
                        e.setInternal(!(link.getHref().startsWith("http://") || link.getHref().startsWith("https://")));
                        e.setOutgoing(false);
                        e.setCrossBook(false);
                        result.add(e);
                    }
                }
            }
        }
    }

    private void outgoingCrossBookLinks() {
        BooksSO books = seite.getBook().getWorkspace().getBooks();
        for (String link : seite.getSeite().getLinks()) {
            int o = link.indexOf("/");
            String folder = link.substring(0, o);
            String id = link.substring(o + 1);
            LinkAnalysisEntry e = new LinkAnalysisEntry();
            try {
                e.setPageTitle(books.byFolder(folder).seiteById(id).getTitle());
            } catch (Exception ex) {
                continue;
            }
            e.setLinkTitle("");
            e.setHref("/s/" + branch + "/" + folder + "/" + id);
            e.setLang("*");
            e.setInternal(true);
            e.setOutgoing(true);
            e.setCrossBook(true);
            result.add(e);
        }
    }

    private void incomingCrossBookLinks() {
        String x = seite.getBook().getBook().getFolder() + "/" + seite.getId();
        for (BookSO b : seite.getBook().getWorkspace().getBooks()) {
            for (SeiteSO s : b.getAlleSeiten()) {
                for (String link : s.getSeite().getLinks()) {
                    if (link.equals(x)) {
                        LinkAnalysisEntry e = new LinkAnalysisEntry();
                        e.setHref("/s/" + branch + "/" + b.getBook().getFolder() + "/" + s.getId());
                        e.setLinkTitle("");
                        e.setPageTitle(s.getTitle());
                        e.setLang("*");
                        e.setInternal(true);
                        e.setOutgoing(false);
                        e.setCrossBook(true);
                        result.add(e);
                    }
                }
            }
        }
    }
}
