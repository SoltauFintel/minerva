package minerva.base;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CSSFilter {

	public String filter(String html) {
		Document document = Jsoup.parse(html);
        Elements elementsWithStyle = document.select("[style]");
        for (Element element : elementsWithStyle) {
            element.removeAttr("style");
        }
		return document.html();
	}
}
