package minerva.export;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

public class HtmlParser {
	// http://jericho.htmlparser.net/samples/console/src/ExtractText.java
	private final String text;
	private final String title;
	
	public HtmlParser(String html, boolean extractText, boolean extractTitle) {
		Source source = new Source(html);
		// Text must be extracted before title for preventing an INFO message!
		if (extractText) {
			text = source.getTextExtractor().toString();
		} else {
			text = null;
		}
		if (extractTitle) {
			Element titleElement = source.getFirstElement(HTMLElementName.TITLE);
			if (titleElement == null) {
				title = "";
			} else {
				// TITLE element never contains other tags so just decode it collapsing
				// whitespace:
				title = CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent());
			}
		} else {
			title = null;
		}
	}
	
	public String getText() {
		if (text == null) {
			throw new UnsupportedOperationException();
		}
		return text;
	}
	
	public String getTitle() {
		if (title == null) {
			throw new UnsupportedOperationException();
		}
		return title;
	}
}
