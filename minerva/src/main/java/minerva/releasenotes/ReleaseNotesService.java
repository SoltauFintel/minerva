package minerva.releasenotes;

import java.io.File;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.config.MinervaFactory;
import minerva.confluence.ConfluenceAccess;
import minerva.confluence.ConfluencePage2;

public class ReleaseNotesService {
	private ConfluenceAccess access;
	
	public ConfluencePage2 loadReleaseNotesPage(String spaceKey, String rootTitle) {
		MinervaFactory fac = MinervaWebapp.factory();
		File imagesFolder = fac.getWorkFolder("release-notes-images");
		String baseUrl = fac.getConfig().getReleaseNotesBaseUrl();
		String token = fac.getConfig().getReleaseNotesToken();
		access = new ConfluenceAccess(baseUrl, token, spaceKey, imagesFolder, baseUrl);
		List<ConfluencePage2> pages = access.searchPages();
		return access.byTitle(rootTitle, pages); // The sub-pages are the Release pages, the sub-sub-pages are the ticket pages.
	}

    public void importAllNonExistingReleases(String spaceKey, String rootTitle) {
        // TODO
    }

	public void importRelease(String spaceKey, String rootTitle, String releaseTitle) {
		ConfluencePage2 root = loadReleaseNotesPage(spaceKey, rootTitle);
		ConfluencePage2 release = root.getSubpages().stream().filter(i -> i.getTitle().equals(releaseTitle))
				.findFirst().orElse(null);
		System.out.println(release.getTitle()); // XXX DEBUG
		for (ConfluencePage2 sub : release.getSubpages()) {
			System.out.println("   --- " + sub.getTitle()); // XXX DEBUG
			access.loadPage(sub);
			for (ConfluencePage2 details : sub.getSubpages()) {
				System.out.println("         --- " + details.getTitle()); // XXX DEBUG
				access.loadPage(details);
			}
		}
		createReleasePages(spaceKey, release);
	}
	
	private void createReleasePages(String spaceKey, ConfluencePage2 release) {
		// TODO Die Kundenseite anlegen      spaceKey
		// TODO Release Nummer ermitteln
		// TODO Die Release.x Seite anlegen
		// TODO die Release Seite anlegen
		// TODO die Ticketseiten anlegen
	}
}
