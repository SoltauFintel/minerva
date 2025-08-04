package gitper.base;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlLinkifier {
    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?<!\\\">|\">)(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)(?!</a>)",
        Pattern.CASE_INSENSITIVE);

	public static String makeLinksClickable(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		Matcher matcher = URL_PATTERN.matcher(input);
		var sb = new StringBuilder();
		while (matcher.find()) {
			String url = matcher.group(1);

			// Wenn die URL schon in einem <a>-Tag ist, nicht ersetzen
			if (isInsideAnchorTag(input, matcher.start())) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(url));
				continue;
			}
			String link = "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
			matcher.appendReplacement(sb, Matcher.quoteReplacement(link));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	// Prüft, ob die Position innerhalb eines bereits bestehenden <a>...</a> liegt
	private static boolean isInsideAnchorTag(String input, int index) {
		int openTag = input.lastIndexOf("<a ", index);
		int closeTag = input.lastIndexOf("</a>", index);
		return openTag != -1 && (closeTag == -1 || closeTag < openTag);
	}
}
