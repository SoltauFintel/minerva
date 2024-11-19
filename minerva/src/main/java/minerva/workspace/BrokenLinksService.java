package minerva.workspace;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    public BLPages load(WorkspaceSO workspace) {
        this.workspace = workspace;
        if (!MinervaOptions.OH_HOSTS.isSet()) {
            throw new RuntimeException("Config option '" + MinervaOptions.OH_HOSTS.getLabel() + "' is not set!");
        }
        String[] hosts = MinervaOptions.OH_HOSTS.get().split("\n");
        List<List<BrokenLink>> sites = new ArrayList<>();
        for (String host : hosts) {
            sites.add(parseMain(host.trim()));
        }
        BLPages ret = merge(sites);
        ret.getPages().sort((a, b) -> StringService.umlaute(a.getTitle()).compareTo(StringService.umlaute(b.getTitle())));
        return ret;
    }
    
    /**
     * @param host protocol, host and port, e.g. "http://host:4490"
     * @throws IOException 
     * @throws MalformedURLException 
     */
    private List<BrokenLink> parseMain(String host) {
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

    /**
     * Broken Links von mehreren Hosts mergen
     * @param brokenLinks -
     * @return BLPages
     */
    private BLPages merge(List<List<BrokenLink>> brokenLinks) {
        BLPages ret = new BLPages();
        for (List<BrokenLink> list : brokenLinks) {
            for (BrokenLink bl : list) {
                for (Entry<String, List<BLCaller>> e : bl.getCallers().entrySet()) {
                    for (BLCaller path : e.getValue()) {
                        save(bl.getUrl(), bl.getErrorType(), bl.getCustomer(), e.getKey(), path.getDetails(), ret.getPages());
                    }
                }
            }
        }
        return ret;
    }

    private void save(String url, String errorType, String customer, String sourceId, String path, List<BLPage> pages) {
        String lang;
        if (path.contains("-> html/en ->")) {
            lang = "en";
        } else {
            lang = "de";
        }
        BLPage page = findPage(sourceId, lang, pages);
        BLLanguage language = page.findLanguage(lang);
        String targetId;
        if (url.startsWith("http://localhost:8080/html/")) {
            targetId = url.substring(url.lastIndexOf("/") + 1);
        } else {
            targetId = url;
        }
        BLBrokenLink bl = language.findBrokenLink(targetId, workspace);
        bl.getCustomers().add(customer);
        bl.setErrorType(errorType);
    }

    private BLPage findPage(String id, String lang, List<BLPage> pages) {
        for (BLPage page : pages) {
            if (page.getId().equals(id)) {
                return page;
            }
        }
        SeiteSO seite = workspace.findPage(id);
        BLPage page = new BLPage(id,
                seite == null ? id : (lang == null ? seite.getTitle() : seite.getSeite().getTitle().getString(lang)),
                seite == null ? "" : seite.getBook().getTitle());
        pages.add(page);
        return page;
    }

    public static class BLPages {
        private final List<BLPage> pages = new ArrayList<>();
        
        public List<BLPage> getPages() {
            return pages;
        }
        
        public int getNumberOfBrokenLinks() {
            int n = 0;
            for (BLPage page : pages) {
                for (BLLanguage l : page.getLanguages()) {
                    n += l.getBrokenLinks().size();
                }
            }
            return n;
        }
    }
    
    public static class BLPage {
        /** Seite ID */
        private final String id;
        private final String title;
        private final String bookTitle;
        private final Set<BLLanguage> languages = new TreeSet<>();
        
        BLPage(String id, String title, String bookTitle) {
            this.id = id;
            this.title = title;
            this.bookTitle = bookTitle;
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
        
        BLLanguage(String language) {
            this.language = language;
        }

        public String getLanguage() {
            return language;
        }

        public List<BLBrokenLink> getBrokenLinks() {
            return brokenLinks;
        }

        public BLBrokenLink findBrokenLink(String id, WorkspaceSO workspace) {
            for (BLBrokenLink bl : brokenLinks) {
                if (bl.getId().equals(id)) {
                    return bl;
                }
            }
            SeiteSO seite = workspace.findPage(id);
            BLBrokenLink bl = new BLBrokenLink(id, seite == null ? id : seite.getSeite().getTitle().getString(language));
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
        private String errorType;
        
        BLBrokenLink(String id, String title) {
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

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }
    }
    
    public static class BrokenLink {
        private final String customer;
        private final String errorType;
        private final String url;
        /** key: caller ID */
        private final Map<String, List<BLCaller>> callers = new HashMap<>();

        BrokenLink(String customer, String errorType, String url) {
            this.customer = customer;
            this.errorType = errorType;
            this.url = url;
        }

        public String getCustomer() {
            return customer;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, List<BLCaller>> getCallers() {
            return callers;
        }
    }
    
    /**
     * Broken Links Caller line
     */
    public static class BLCaller {
        private final String details;

        BLCaller(String details) {
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
            } else {
                // "http://localhost:8080/html/93783143.html"
                final String x = "http://localhost:8080/html/";
                o = details.indexOf(x);
                if (o == 0) {
                    if (details.indexOf(x, x.length()) < 0) {
                        return details.substring(x.length()).replace(".html", "");
                    }
                }
            }
            return details;
        }
    }
}
