package minerva.releasenotes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.base.NlsString;
import minerva.config.MinervaFactory;
import minerva.confluence.ConfluenceAccess;
import minerva.confluence.ConfluencePage2;
import minerva.model.SeiteSO;

public class ReleaseNotesService {
	private ConfluenceAccess access;
	private ReleaseNotesContext ctx;
	
	// TODO createSeite syntax schöner machen
	
	public ReleaseNotesService(ReleaseNotesContext ctx) {
	    this.ctx = ctx;
	}
	
	public ConfluencePage2 loadReleaseNotesPage(String spaceKey, String rootTitle) {
	    // ctx is null
		MinervaFactory fac = MinervaWebapp.factory();
		File imagesFolder = fac.getWorkFolder("release-notes-images");
		String baseUrl = fac.getConfig().getReleaseNotesBaseUrl();
		String token = fac.getConfig().getReleaseNotesToken();
		access = new ConfluenceAccess(baseUrl, token, spaceKey, imagesFolder, baseUrl);
		List<ConfluencePage2> pages = access.searchPages();
		return access.byTitle(rootTitle, pages); // The sub-pages are the Release pages, the sub-sub-pages are the ticket pages.
	}

    public void importAllNonExistingReleases() {
        // TODO
    }

	public String importRelease() {
		ConfluencePage2 root = loadReleaseNotesPage(ctx.getSpaceKey(), ctx.getRootTitle());
		ConfluencePage2 release = root.getSubpages().stream().filter(i -> i.getTitle().equals(ctx.getReleaseTitle())).findFirst().orElse(null);
		ctx.setReleasePage(release);
		for (ConfluencePage2 sub : release.getSubpages()) {
			access.loadPage(sub);
			for (ConfluencePage2 details : sub.getSubpages()) {
				access.loadPage(details);
			}
		}
		createReleasePages();
		return ctx.getResultingReleasePage().getId();
	}
	
	// TODO gucken, ob es überhaupt Ticketseiten gibt
	private void createReleasePages() {
	    createCustomerPage();
	    String releaseNumber = getReleaseNumber(ctx.getReleasePage().getTitle());
        createReleaseSectionPage(releaseNumber);
	    createReleasePage();
	    createTicketPages();
        ctx.getBook().dao().saveFiles(ctx.getFiles(),
                new CommitMessage("Release Notes " + ctx.getSpaceKey() + " " + releaseNumber),
                ctx.getBook().getWorkspace());
        Logger.info(releaseNumber +" | Number of saved pages: " + ctx.getFiles().keySet().stream().filter(i -> i.endsWith(".meta")).count());
    }

    private void createCustomerPage() {
        SeiteSO customerPage = findCustomerPage();
        if (customerPage == null) {
            customerPage = ctx.getBook().getSeiten().createSeite(ctx.getBook().getISeite(), ctx.getBook(), IdGenerator.createId6());
            customerPage.getSeite().getTags().add(tag());
            customerPage.getSeite().getTags().add("reversed-order");
            customerPage.getSeite().setSorted(true);
            setTitleAndDummyContent(customerPage, "Programmänderungen " + ctx.getSpaceKey(), "Release Notes " + ctx.getSpaceKey());
            customerPage.getSeite().setTocSubpagesLevels(1);
            customerPage.saveMetaTo(ctx.getFiles());
            customerPage.saveHtmlTo(ctx.getFiles(), langs());
        }
        ctx.setCustomerPage(customerPage);
    }

    private SeiteSO findCustomerPage() {
        return ctx.getBook().getSeiten()._byTag(tag());
    }

    private String tag() {
        return "release-notes-" + ctx.getSpaceKey().toLowerCase();
    }

    private String getReleaseNumber(final String pTitle) {
        String title = " " + pTitle + " ";
        // searching for blank + digit[1-9] + dot ...
        int o = title.indexOf(".");
        while (o >= 2) {
            if (title.charAt(o - 2) == ' ' && title.charAt(o - 1) >= '1' && title.charAt(o - 1) <= '9') {
                int oo = title.indexOf(" ", o);
                if (oo > o) {
                    return title.substring(o - 1, oo);
                }
            }
            o = title.indexOf(".", o + 1);
        }
        return ""; // can't extract release number
    }

    // Create release section page "3.26.x" for release "3.26.7".
    private void createReleaseSectionPage(String releaseNumber) {
        String title = section(releaseNumber);
        SeiteSO sectionPage = findReleaseSectionPage(title);
        if (sectionPage == null) {
            sectionPage = ctx.getCustomerPage().getSeiten().createSeite(ctx.getCustomerPage(), ctx.getBook(), IdGenerator.createId6());
            setTitleAndDummyContent(sectionPage, title, title);
            sectionPage.getSeite().setTocSubpagesLevels(1);
            sectionPage.getSeite().getTags().add("reversed-order");
            sectionPage.getSeite().setSorted(true);
            sectionPage.saveMetaTo(ctx.getFiles());
            sectionPage.saveHtmlTo(ctx.getFiles(), langs());
        } // else: Release number can't be extracted or has a special format. Then omit section page.
        ctx.setReleaseSectionPage(sectionPage);
    }
    
    private SeiteSO findReleaseSectionPage(String title) {
        return title == null ? null : ctx.getCustomerPage().getSeiten(ctx.getLang())._byTitle(title, ctx.getLang());
    }
    
    private String section(String releaseNumber) {
        int o = releaseNumber.lastIndexOf(".");
        return o >= 0 ? (releaseNumber.substring(0, o + 1) + "x") : null;
    }

    private void createReleasePage() {
        SeiteSO parent = ctx.getReleaseSectionPage() == null ? ctx.getCustomerPage() : ctx.getReleaseSectionPage();
        SeiteSO releasePage = parent.getSeiten().createSeite(parent, ctx.getBook(), IdGenerator.createId6());
        releasePage.getSeite().getTitle().setString("de", ctx.getReleasePage().getTitle());
        releasePage.getSeite().getTitle().setString("en", ctx.getReleasePage().getTitle());
        releasePage.getContent().setString(ctx.getLang(), "<p>.</p>");
        releasePage.getSeite().setTocSubpagesLevels(1);
        releasePage.saveMetaTo(ctx.getFiles());
        releasePage.saveHtmlTo(ctx.getFiles(), langs());
        ctx.setResultingReleasePage(releasePage);
System.out.println("--Release page angelegt: " + releasePage.getTitle());        
    }

    private void createTicketPages() {
        for (ConfluencePage2 src : ctx.getReleasePage().getSubpages()) {
            SeiteSO seite = ctx.getResultingReleasePage().getSeiten().createSeite(ctx.getResultingReleasePage(), ctx.getBook(), IdGenerator.createId6());
            seite.getSeite().getTitle().setString("de", src.getTitle());
            seite.getSeite().getTitle().setString("en", src.getTitle());
            String html = src.getHtml();
            for (ConfluencePage2 details : src.getSubpages()) {
                html += details.getHtml();
            }
            if (html.isBlank()) {
                html = "<p>.</p>";
            }
            seite.getContent().setString(ctx.getLang(), html);
            seite.saveMetaTo(ctx.getFiles());
            seite.saveHtmlTo(ctx.getFiles(), langs());
        }
    }

    private void setTitleAndDummyContent(SeiteSO seite, String titleDE, String titleEN) {
        NlsString title = seite.getSeite().getTitle();
        title.setString("de", titleDE);
        title.setString("en", titleEN);
        NlsString content = seite.getContent();
        content.setString("de", "de".equals(ctx.getLang()) ? "<p>.</p>" : ""); // "."=page not empty (will be displayed),
        content.setString("en", "en".equals(ctx.getLang()) ? "<p>.</p>" : ""); // "" =page empty (will not be displayed)
    }

    public static List<String> langs() {
        List<String> langs = new ArrayList<>();
        langs.add("de");
        langs.add("en");
        return langs;
    }
    
    public List<String> getExistingReleasePages() {
        List<String> titles = new ArrayList<>();
        SeiteSO customerPage = findCustomerPage();
        if (customerPage != null) {
            for (SeiteSO sectionPage : customerPage.getSeiten()) {
                if (sectionPage.getSeiten().isEmpty()) { // If no subpages it's a release page.
                    titles.add(sectionPage.getSeite().getTitle().getString(ctx.getLang()));
                } else { // Subpages are release pages.
                    sectionPage.getSeiten().forEach(seite -> titles.add(seite.getSeite().getTitle().getString(ctx.getLang())));
                }
            }
        }
        return titles;
    }
}
