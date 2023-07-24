package minerva.subscription;

import java.util.ArrayList;
import java.util.List;

import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;

public class BrokenMappingsService {
	private final PageTitles titles;
	private final List<String> langs;
	
	public BrokenMappingsService(List<String> langs) {
		titles = new SubscriptionService().loadPageTitles();
		this.langs = langs;
	}

	public List<BrokenMapping> getBrokenMappings(WorkspaceSO workspace) {
		List<BrokenMapping> ret = new ArrayList<>();
		for (BookSO book : workspace.getBooks()) {
			collectBrokenMappings(book.getSeiten(), ret);
		}
		return ret;
	}

	private void collectBrokenMappings(SeitenSO seiten, List<BrokenMapping> brokenMappings) {
		for (SeiteSO seite : seiten) {
			for (String key : seite.getSeite().getHelpKeys()) {
				if (!find(key)) {
					brokenMappings.add(new BrokenMapping(seite, key));
				}
			}
			
			collectBrokenMappings(seite.getSeiten(), brokenMappings); // recursive
		}
	}
	
	private boolean find(String key) {
		if (key.endsWith("!")) {
			key = key.substring(0, key.length() - "!".length());
		}
		for (String lang : langs) {
			for (PageTitle pageTitle : titles.getLang().get(lang)) {
				if (pageTitle.getId().equals(key)) {
					return true;
				}
			}
		}
		return false;
	}

	public static class BrokenMapping {
		private final SeiteSO seite;
		private final String key;

		public BrokenMapping(SeiteSO seite, String key) {
			this.seite = seite;
			this.key = key;
		}

		public SeiteSO getSeite() {
			return seite;
		}

		public String getKey() {
			return key;
		}
	}
}
