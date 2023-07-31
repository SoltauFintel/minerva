package minerva.migration;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import minerva.base.StringService;

public class ConfluenceComment {
    private String pageId;
    /** forename familyname */
    private String author;
    /** yyyy-mm-ddThh:mm:ss */
    private String created;
    private String html;
    private final List<ConfluenceComment> comments = new ArrayList<>();
    private final transient List<String> persons = new ArrayList<>();

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public List<ConfluenceComment> getComments() {
        return comments;
    }
    
    public String getPlainText() {
    	persons.clear();
        if (StringService.isNullOrEmpty(html)) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        String html2 = html;
        // mentions
        for (Element e : doc.selectXpath("//a[@class='confluence-userlink user-mention']")) {
            String personName = e.text();
			html2 = html2.replace(from(e), "@[" + personName + "]");
            persons.add(personName);
        }
        // links
        for (Element e : doc.selectXpath("//a[not(@class='confluence-userlink user-mention')]")) {
            String href = e.attr("href");
            int o = href.indexOf("pageId="); // It's a link to another Confluence page.
            if (o >= 0) {
                href = href.substring(o + "pageId=".length());
            }
            html2 = html2.replace(from(e), "(" + e.text() + ")[" + href + "]");
        }
        // make nice formatted non-HTML text
        String ret = new HtmlToPlainText().getPlainText(Jsoup.parse(html2).root());
        while (ret.startsWith("\n")) { // remove empty line at begin
            ret = ret.substring(1);
        }
        while (ret.endsWith("\n")) { // remove empty lines at end
        	ret = ret.substring(0, ret.length() - 1);
        }
		int ni = doc.selectXpath("//img").size();
		if (ni > 0) {
			ret = "(Kommentar enthielt " + (ni == 1 ? "eine Grafik" : "Grafiken") + ".)\n" + ret;
		}
        return ret;
    }
    
    private String from(Element e) {
        return e.toString() //
                .replace("ü", "&uuml;").replace("ä", "&auml;").replace("ö", "&ouml;") //
                .replace("Ü", "&Uuml;").replace("Ä", "&Auml;").replace("Ö", "&Ouml;") //
                .replace("ß", "&szlig;");
    }

	public List<String> getPersons() {
		return persons;
	}
}
