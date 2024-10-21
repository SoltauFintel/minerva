package minerva.mask;

public class WebpageTitleService {
	public static WebpageTitleService webpageTitleService = new WebpageTitleService();
	private static final int MAX_TITLE_LENGTH = 70;

	public String getTitle(String url) {
		if (url == null || url.isBlank()) {
			return url;
		}
		return shorten(url);
	}
	
	protected String shorten(String title) {
		return title.length() > MAX_TITLE_LENGTH + 3 ? (title.substring(0, MAX_TITLE_LENGTH) + "...") : title;
	}
}
