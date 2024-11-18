package minerva.workspace;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BrokenLinksService {
    private static final int TIMEOUT = 10000;

    /**
     * @param host protocol, host and port, e.g. "http://host:4490"
     */
    public BrokenLinksSite examine(String host) {
        try {
            return parseMain(host, Jsoup.parse(new URL(host + "/brokenlinks"), TIMEOUT));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BrokenLinksSite parseMain(String host, Document doc) throws Exception {
        BrokenLinksSite ret = new BrokenLinksSite();
        Elements e = doc.select("ul");
        if (e.size() > 0) {
            for (Element ee : e.get(0).getElementsByTag("li")) {
                Elements a = ee.select("a");
                String href = a.get(0).attr("href");
                int o = href.indexOf("brokenlinks-details?id=");
                if (o >= 0) {
                    String id = href.substring(o + "brokenlinks-details?id=".length());
                    String url = host + "/brokenlinks-details?id=" + id;
                    Document doc2 = Jsoup.parse(new URL(url), TIMEOUT);
                    BrokenLink bl = new BrokenLink( //
                            doc2.select("h1").get(0).text(), // "Broken Links <customer>"
                            doc2.select("h2").get(0).text(), // "error: page not found (404)"
                            doc2.select("h2").get(1).text()); // "http://localhost:8080/html/32999748"
                    Elements f = doc2.select("ul");
                    for (Element ff : f.get(0).getElementsByTag("li")) {
                        bl.getCallers().add(ff.text());
                        // "$root -> html/de -> html/scp3p6.html -> /html/handbuch -> html/32999685.html"
                    }
                    ret.getBrokenLinks().add(bl);
                }
            }
        }
        return ret;
    }
}
