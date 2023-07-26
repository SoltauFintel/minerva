package minerva.seite;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.publish.TocEntry;

public class TocMacro {
    private final SeiteSO seite;
    private final String lang;
    private final String customer;
    private String toc = null;
    
    public TocMacro(SeiteSO seite, String lang, String customer) {
        this.seite = seite;
        this.lang = lang;
        this.customer = customer;
    }

    public String transform(String html) {
        toc = "";
        int headingslevels = seite.getSeite().getTocHeadingsLevels();
        int subpagesLevels = seite.getSeite().getTocSubpagesLevels();
        if (headingslevels == 0 && subpagesLevels == 0) {
            return html; // nothing to do
        }
        
        Document doc = Jsoup.parse(html);
        Elements headings = doc.select("h2,h3,h4,h5,h6");
        List<TocEntry> entries = new ArrayList<>();
        if (headingslevels > 0) {
            collectTocEntries(headingslevels, headings, entries);
        }
        
        final int nHeadings = entries.size();
        subpages2TocEntries(seite.getSeiten(), entries, 1, subpagesLevels);
        
        if (!entries.isEmpty()) {
            toc = "<div class=\"toc\">" + makeTocHtml(entries, nHeadings) + "</div>";
        }
        return doc.html();
    }

    private void collectTocEntries(int headingslevels, Elements headings, List<TocEntry> entries) {
        TocEntry h2 = null, h3 = null, h4 = null, h5 = null;
        int lfd = 0;
        for (Element heading : headings) {
            int level = Integer.parseInt(heading.nodeName().substring(1, 2));
            if (headingslevels < level - 1) {
                continue;
            }
            String title = heading.text();
            TocEntry entry = new TocEntry();
            entry.setTitle(title);
            entry.setId("#t" + ++lfd);
            heading.attr("id", entry.getId().substring("#".length())); // modify HTML
            
            if (level == 6 && h5 != null) {
                h5.getSubpages().add(entry);
            } else if (level == 5 && h4 != null) {
                h4.getSubpages().add(entry);
                h5 = entry;
            } else if (level == 4 && h3 != null) {
                h3.getSubpages().add(entry);
                h4 = entry;
            } else if (level == 3 && h2 != null) {
                h2.getSubpages().add(entry);
                h3 = entry;
            } else if (level == 2) {
                entries.add(entry);
                h2 = entry;
            } else {
                entries.add(entry);
            }
        }
    }

    private void subpages2TocEntries(SeitenSO seiten, List<TocEntry> entries, int level, int maxLevel) {
        if (level > maxLevel) {
            return;
        }
        for (SeiteSO seite : seiten) {
            if (seite.isVisible(customer, lang).isVisible()) {
                TocEntry entry = new TocEntry();
                entry.setId(seite.getId());
                entry.setTitle(seite.getSeite().getTitle().getString(lang));
                entries.add(entry);
                subpages2TocEntries(seite.getSeiten(), entry.getSubpages(), level + 1, maxLevel); // recursive
            }
        }
    }

    private String makeTocHtml(List<TocEntry> entries, int nHeadings) {
        String ret = "";
        int n = entries.size();
        if (n > 0) {
            ret = "\n<ul class=\"toc\">";
        }
        for (int i = 0; i < n; i++) {
            TocEntry entry = entries.get(i);
            String cls = "";
            if (nHeadings >= 0 && i >= nHeadings) {
                cls = " class=\"subpage\"";
                nHeadings = -1;
            }
            ret += "<li" + cls + " style=\"list-style-type: square;\">" //
                    + "<a href=\"" + entry.getId() + "\">" + entry.getTitle() + "</a>" //
                    + makeTocHtml(entry.getSubpages(), -1) // recursive 
                    + "</li>";
        }
        if (n > 0) {
            ret += "</ul>\n";
        }
        return ret;
    }

    /**
     * Must be called after transform().
     * @return TOC HTML
     */
    public String getTOC() {
        if (toc == null) {
            throw new RuntimeException("Call transform() before getTOC()!");
        }
        return toc;
    }
}
