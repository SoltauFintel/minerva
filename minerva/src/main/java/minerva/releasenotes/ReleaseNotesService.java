package minerva.releasenotes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.NLS;
import minerva.base.NlsString;
import minerva.config.MinervaFactory;
import minerva.confluence.ConfluenceAccess;
import minerva.confluence.ConfluencePage2;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.validate.ValidatorService;

public class ReleaseNotesService {
	private ConfluenceAccess access;
	private ReleaseNotesContext ctx;
	
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
        ConfluencePage2 root = loadReleaseNotesPage(ctx.getSpaceKey(), ctx.getRootTitle());
        List<String> existing = getExistingReleasePages();
        for (ConfluencePage2 release : root.getSubpages()) {
            if (!release.getSubpages().isEmpty() && !existing.contains(release.getTitle())) {
                importRelease2(release);
            }
        }
    }

	public String importRelease() {
		ConfluencePage2 root = loadReleaseNotesPage(ctx.getSpaceKey(), ctx.getRootTitle());
		ConfluencePage2 release = root.getSubpages().stream().filter(i -> i.getId().equals(ctx.getReleaseId())).findFirst().orElse(null);
		if (release == null) {
            Logger.info("Can't import release notes because release page is not found. Release ID: " + ctx.getReleaseId());
            return null;
		} else if (release.getSubpages().isEmpty()) {
		    Logger.info(release.getTitle() + " | No import because there are no ticket pages.");
		    return null;
		}
		return importRelease2(release);
	}

    private String importRelease2(ConfluencePage2 release) {
        Logger.info("importing releaste notes... | " + release.getTitle());
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
	
	private void createReleasePages() {
	    createCustomerPage();
	    String releaseNumber = getReleaseNumber(ctx.getReleasePage().getTitle());
        createSectionPage(releaseNumber);
	    createReleasePage();
        ctx.getBook().dao().saveFiles(ctx.getFiles(),
                new CommitMessage("Release Notes " + ctx.getSpaceKey() + " " + releaseNumber),
                ctx.getBook().getWorkspace());
        Logger.info(releaseNumber +" | Number of saved pages: " + ctx.getFiles().keySet().stream().filter(i -> i.endsWith(".meta")).count());
    }

    private void createCustomerPage() {
        SeiteSO customerPage = findCustomerPage();
        if (customerPage == null) {
            customerPage = createSeite(ctx.getBook());
            customerPage.getSeite().getTags().add("release-notes");
            customerPage.getSeite().getTags().add(tag());
            customerPage.getSeite().getTags().add("reversed-order");
            customerPage.getSeite().setSorted(true);
            String customer = ctx.getConfig().getCustomer();
            setTitleAndDummyContent(customerPage, "Programmänderungen " + customer, "Release Notes " + customer);
            customerPage.getSeite().setTocSubpagesLevels(2);
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
    private void createSectionPage(String releaseNumber) {
        String title = section(releaseNumber);
        if (title == null) {
            ctx.setSectionPage(null);
            return;
        }
        SeiteSO sectionPage = findSectionPage(title);
        if (sectionPage == null) {
            sectionPage = createSeite(ctx.getCustomerPage());
            setTitleAndDummyContent(sectionPage, title, title);
            sectionPage.getSeite().setTocSubpagesLevels(1);
            sectionPage.getSeite().getTags().add("reversed-order");
            sectionPage.getSeite().setSorted(true);
            sectionPage.saveMetaTo(ctx.getFiles());
            sectionPage.saveHtmlTo(ctx.getFiles(), langs());
            ctx.getCustomerPage().getSeiten(ctx.getLang()); // sort
        } // else: Release number can't be extracted or has a special format. Then omit section page.
        ctx.setSectionPage(sectionPage);
    }

    private SeiteSO findSectionPage(String title) {
        return title == null ? null : ctx.getCustomerPage().getSeiten(ctx.getLang())._byTitle(title, ctx.getLang());
    }
    
    private String section(String releaseNumber) {
        int o = releaseNumber.lastIndexOf(".");
        return o >= 0 ? (releaseNumber.substring(0, o + 1) + "x") : null;
    }

    private void createReleasePage() {
        SeiteSO parent = ctx.getSectionPage() == null ? ctx.getCustomerPage() : ctx.getSectionPage();
        SeiteSO releasePage = createSeite(parent);
        ctx.setResultingReleasePage(releasePage);
        releasePage.getSeite().getTitle().setString("de", ctx.getReleasePage().getTitle());
        releasePage.getSeite().getTitle().setString("en", ctx.getReleasePage().getTitle());
        releasePage.getContent().setString(ctx.getLang(), getReleasePageContent());
        releasePage.getSeite().setTocHeadingsLevels(2);
        releasePage.saveMetaTo(ctx.getFiles());
        releasePage.saveHtmlTo(ctx.getFiles(), langs());
        parent.getSeiten(ctx.getLang()); // sort
    }
    
    private String getReleasePageContent() {
        List<String> part1 = new ArrayList<>();
        List<String> part2 = new ArrayList<>();
        ValidatorService v = new ValidatorService();
        for (ConfluencePage2 src : ctx.getReleasePage().getSubpages()) {
            String body = v.removeEmptyLinesAtEnd(src.getHtml());
            for (ConfluencePage2 details : src.getSubpages()) {
                body += v.removeEmptyLinesAtEnd(details.getHtml());
            }
            String html = "<h3>" + src.getTitle() + "</h3>"
                    + processImages(body, ctx.getResultingReleasePage());
            if (src.getTitle().contains(ctx.getConfig().getTicketPrefix())) { // customer-specific
                part1.add(html);
            } else { // general
                part2.add(html);
            }
        }
        return part2html(part1, ctx.getConfig().getCustomer())
                + part2html(part2, NLS.get(ctx.getLang(), "generalChanges"));
    }

    private String processImages(String html, SeiteSO seite) {
        Set<String> images = ConfluenceAccess.extract(html, "img", "src");
        File imagesFolder = MinervaWebapp.factory().getWorkFolder("release-notes-images");
        for (String img : images) {
            if (img.startsWith("img/")) {
                File imgFile = new File(imagesFolder, img.substring("img/".length()));
                if (imgFile.isFile()) {
                    File targetFolder = new File(ctx.getBook().getFolder(), "img/" + seite.getId());
                    FileService.copyFile(imgFile, targetFolder);
                    imgFile.delete();
                    html = html.replace(img, "img/" + seite.getId() + "/" + imgFile.getName());
                    ctx.getFiles().put(seite.filenameImage("img/" + seite.getId() + "/" + imgFile.getName()),
                            DirAccess.IMAGE);
                } else {
                    throw new RuntimeException("File not found: " + imgFile.getAbsolutePath() + "\nin HTML: " + img);
                }
            }
        }
        return html;
    }

    private String part2html(List<String> part, String title) {
        return (part.isEmpty() ? "" : ("<h2>" + title + "</h2>"))
                + part.stream().sorted().collect(Collectors.joining());
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
    
    private SeiteSO createSeite(SeiteSO parent) {
        return parent.getSeiten().createSeite(parent, parent.getBook(), IdGenerator.createId6());
    }

    private SeiteSO createSeite(BookSO parent) {
        return parent.getSeiten().createSeite(parent.getISeite(), parent, IdGenerator.createId6());
    }
}
