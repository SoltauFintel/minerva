package minerva.workspace;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gitper.base.StringService;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class BrokenLinksService {
    private static final int TIMEOUT = 10000;
    private WorkspaceSO workspace;
    
    public List<BLPage> load(WorkspaceSO workspace) {
        this.workspace = workspace;
        if (!MinervaOptions.OH_HOSTS.isSet()) {
            throw new RuntimeException("Config option 'OH_HOSTS' is not set!");
        }
        String[] hosts = MinervaOptions.OH_HOSTS.get().split("\n");
        List<List<BrokenLink>> sites = new ArrayList<>();
        for (String host : hosts) {
            sites.add(parseMain(host.trim()));
        }
        List<BLPage> ret = merge(sites);
        ret.sort((a, b) -> StringService.umlaute(a.getTitle()).compareTo(StringService.umlaute(b.getTitle())));
        return ret;
    }
    
    /**
     * @param host protocol, host and port, e.g. "http://host:4490"
     * @throws IOException 
     * @throws MalformedURLException 
     */
    public List<BrokenLink> parseMain(String host) {
        try {
            List<BrokenLink> ret = new ArrayList<>();
            Document doc = Jsoup.parse(new URL(host + "/brokenlinks"), TIMEOUT);
            Elements e = doc.select("ul");
            if (!e.isEmpty()) {
                for (Element ee : e.get(0).getElementsByTag("li")) {
                    Elements a = ee.select("a");
                    if (a.isEmpty()) {
                        continue;
                    }
                    String href = a.get(0).attr("href");
                    int o = href.indexOf("brokenlinks-details?id=");
                    if (o < 0) {
                        continue;
                    }
                    String id = href.substring(o + "brokenlinks-details?id=".length());
                    BrokenLink bl = parseDetails(host, id);
                    if (bl != null) {
                        ret.add(bl);
                    }
                }
            }
            return ret;
        } catch (IOException e) {
            throw new RuntimeException("Error accessing " + host, e);
        }
    }

    private BrokenLink parseDetails(String host, String id) throws MalformedURLException, IOException {
        Document doc = Jsoup.parse(new URL(host + "/brokenlinks-details?id=" + id), TIMEOUT);
        Elements h1 = doc.select("h1");
        Elements h2 = doc.select("h2");
        if (h1.isEmpty() || h2.size() < 2) {
            return null;
        }
        BrokenLink bl = new BrokenLink( //
                h1.get(0).text().replace("Broken Links ", ""), // "Broken Links <customer>" -> customer
                h2.get(0).text(), // "error: page not found (404)"
                h2.get(1).text()); // "http://localhost:8080/html/32999748"
        Elements f = doc.select("ul");
        if (!f.isEmpty()) {
            for (Element ff : f.get(0).getElementsByTag("li")) {
                BLCaller caller = new BLCaller(ff.text());
                // "$root -> html/de -> html/scp3p6.html -> /html/handbuch -> html/32999685.html"
                String cid = caller.getId();
                List<BLCaller> list = bl.getCallers().get(cid);
                if (list == null) {
                    list = new ArrayList<>();
                    bl.getCallers().put(cid, list);
                }
                list.add(caller);
            }
        }
        return bl;
    }

    // Broken Links von mehreren Hosts mergen
    public List<BLPage> merge(List<List<BrokenLink>> brokenLinks) {
        List<BLPage> pages = new ArrayList<>();
        for (List<BrokenLink> list : brokenLinks) {
            for (BrokenLink bl : list) {
                if (bl.getErrorType().contains("(404)") && bl.getUrl().startsWith("http://localhost:8080/html/")) {
                    for (Entry<String, List<BLCaller>> e : bl.getCallers().entrySet()) {
                        for (BLCaller path : e.getValue()) {
                            save(bl.getUrl(), bl.getCustomer(), e.getKey(), path.getDetails(), pages);
                        }
                    }
                }
            }
        }
        return pages;
    }

    private void save(String url, String customer, String sourceId, String path, List<BLPage> pages) {
        String lang;
        if (path.contains("-> html/de ->")) {
            lang = "de";
        } else if (path.contains("-> html/en ->")) {
            lang = "en";
        } else {
            throw new RuntimeException("Cannot extract language from: " + path);
        }
        BLPage page = findPage(sourceId, lang, pages);
        BLLanguage language = page.findLanguage(lang);
        String targetId = url.substring(url.lastIndexOf("/") + 1);
        BLBrokenLink bl = language.findBrokenLink(targetId, this, workspace);
        bl.getCustomers().add(customer);
    }

    private BLPage findPage(String id, String lang, List<BLPage> pages) {
        for (BLPage p : pages) {
            if (p.getId().equals(id)) {
                return p;
            }
        }
        BLPage p = new BLPage(id, getTitle(id, null));
        SeiteSO seite = workspace.findPage(id);
        p.setBookTitle(seite == null ? "" : seite.getBook().getTitle());
        pages.add(p);
        return p;
    }

    String getTitle(String id, String lang) {
        SeiteSO seite = workspace.findPage(id);
        if (seite == null) {
            return id;
        }
        return lang == null ? seite.getTitle() : seite.getSeite().getTitle().getString(lang);
    }
    
    public static class BLPage {
        /** Seite ID */
        private final String id;
        private final String title;
        private final Set<BLLanguage> languages = new TreeSet<>();
        private String bookTitle;
        
        public BLPage(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }
        
        public String getTitle() {
            return title;
        }

        public Set<BLLanguage> getLanguages() {
            return languages;
        }
        
        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public BLLanguage findLanguage(String language) {
            for (BLLanguage l : languages) {
                if (l.getLanguage().equals(language)) {
                    return l;
                }
            }
            BLLanguage l = new BLLanguage(language);
            languages.add(l);
            return l;
        }
    }
    
    public static class BLLanguage implements Comparable<BLLanguage> {
        /** "de", "en" */
        private final String language;
        private final List<BLBrokenLink> brokenLinks = new ArrayList<>();
        
        public BLLanguage(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }

        public List<BLBrokenLink> getBrokenLinks() {
            return brokenLinks;
        }

        public BLBrokenLink findBrokenLink(String id, BrokenLinksService sv, WorkspaceSO workspace) {
            for (BLBrokenLink bl : brokenLinks) {
                if (bl.getId().equals(id)) {
                    return bl;
                }
            }
            BLBrokenLink bl = new BLBrokenLink(id, sv.getTitle(id, language));
            SeiteSO seite = workspace.findPage(id);
            if (seite != null) {
                bl.setTags(seite.getSeite().getTags());
                bl.setBookFolder(seite.getBook().getBook().getFolder());
            }
            brokenLinks.add(bl);
            brokenLinks.sort((a, b) -> StringService.umlaute(a.getTitle()).compareTo(StringService.umlaute(b.getTitle())));
            return bl;
        }

        @Override
        public int compareTo(BLLanguage o) {
            return this.language.compareTo(o.language);
        }
    }
    
    public static class BLBrokenLink {
        private final Set<String> customers = new TreeSet<>();
        /** Seite ID */
        private final String id;
        private final String title;
        private Set<String> tags = Set.of();
        private String bookFolder = "";

        public BLBrokenLink(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Set<String> getCustomers() {
            return customers;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public String getBookFolder() {
            return bookFolder;
        }

        public void setBookFolder(String bookFolder) {
            this.bookFolder = bookFolder;
        }
    }
    
    /**
     * Broken Links Caller line
     */
    public static class BLCaller {
        private final String details;

        public BLCaller(String details) {
            this.details = details;
        }

        public String getDetails() {
            return details;
        }

        public String getId() {
            int o = details.lastIndexOf("-> ");
            if (o >= 0) {
                o += "-> ".length();
                String ret = details.substring(o).replace("html/", "").replace(".html", "");
                if (ret.startsWith("/")) {
                    ret = ret.substring(1);
                }
                return ret;
            }
            return details;
        }
    }
}
