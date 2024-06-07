package minerva.validate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import minerva.model.SeiteSO;
import minerva.seite.link.Link;

public class ValidationResult {
	private final List<VRSeite> seiten = new ArrayList<>();
	private final List<VRLink> links = new ArrayList<>();
	private final List<VRUnusedImageSeite> unusedImages = new ArrayList<>();

	public List<VRSeite> getSeiten() {
		return seiten;
	}
	
	public int getMessagesCount() {
		return seiten.stream().mapToInt(s -> s.getFehlerliste().size()).sum();
	}
	
	public int getSeitenCount() {
		HashSet<String> set = new HashSet<>();
		seiten.forEach(s -> set.add(s.getId()));
		return set.size();
	}

	public List<VRLink> getLinks() {
		return links;
	}

	public List<VRUnusedImageSeite> getUnusedImages() {
		return unusedImages;
	}

	public static class VRSeite {
		private final String lang;
		private final String id;
		private final String title;
		private final String link;
		private final List<String> fehlerliste;

		public VRSeite(SeiteSO seite, String lang, List<String> fehlerliste) {
			this.lang = lang;
			id = seite.getId();
			title = seite.getSeite().getTitle().getString(lang);
			link = seite.viewlink();
			this.fehlerliste = fehlerliste;
		}

		public String getLang() {
			return lang;
		}

		public String getId() {
			return id;
		}

		public String getTitle() {
			return title;
		}

		public String getLink() {
			return link;
		}

		public List<String> getFehlerliste() {
			return fehlerliste;
		}
	}

	public static class VRLink {
		private final String lang;
		private final Link link;
		private final SeiteSO seite;

		public VRLink(String lang, Link link, SeiteSO seite) {
			this.lang = lang;
			this.link = link;
			this.seite = seite;
		}

		public String getLang() {
			return lang;
		}

		public String getHref() {
			return link.getHref();
		}

		public String getTitle() {
			return link.getTitle();
		}

		public String getPagelink() {
			return seite.viewlink();
		}

		public String getPagetitle() {
			return seite.getTitle();
		}
	}

	public static class VRUnusedImageSeite {
		private final String title;
		private final String link;
		/** image filenames */
		private final List<String> unusedImages = new ArrayList<>();
		
		public VRUnusedImageSeite(SeiteSO seite) {
			title = seite.getSeite().getTitle().getString(seite.getBook().getWorkspace().getUser().getGuiLanguage());
			link = seite.viewlink();
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getLink() {
			return link;
		}
		
		public List<String> getUnusedImages() {
			return unusedImages;
		}
	}
}
