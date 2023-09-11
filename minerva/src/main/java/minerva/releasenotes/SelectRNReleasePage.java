package minerva.releasenotes;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.book.BPage;
import minerva.confluence.ConfluencePage2;

/**
 * Release notes: select release to be imported
 */
public class SelectRNReleasePage extends BPage {

	@Override
	protected void execute() {
		// TODO MinervaWebapp.factory().gitlabOnlyPage();
		String spaceKey = ctx.queryParam("s");
		String rootTitle = ctx.queryParam("t");
		String lang = ctx.queryParam("l");
		if (spaceKey == null || rootTitle == null || rootTitle.isEmpty() || lang == null) {
			throw new RuntimeException("Missing parameters");
		}
		if (isPOST()) {
			importRelease(spaceKey, rootTitle, lang);
		} else {
			displayFormular(spaceKey, rootTitle, lang);
		}
	}

    private void displayFormular(String spaceKey, String rootTitle, String lang) {
        ConfluencePage2 rnpage = new ReleaseNotesService(null).loadReleaseNotesPage(spaceKey, rootTitle);
        if (rnpage == null) {
        	throw new UserMessage("pageDoesntExist", user, s -> s.replace("$t", rootTitle));
        }

        List<String> releases = rnpage.getSubpages().stream().map(i -> i.getTitle()).collect(Collectors.toList());
        List<String> existingReleasePageTitles = new ReleaseNotesService(new ReleaseNotesContext(spaceKey, null, null, book, lang)).getExistingReleasePages();
        releases.removeIf(title -> existingReleasePageTitles.contains(title));
        releases.add(n("alleNochNichtVorhandenen"));

        header(n("loadReleaseNotes"));
        ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
        initColumnFormularGenerator(gen);
        combobox("releases", releases, "", false, model); // TODO Amalia: ID
        TemplatesInitializer.fp.setContent(gen
        		.combobox("release", n("Release"), 5, "releases", true)
                .save(n("Import"))
                .getHTML(booklink + "/rn-select-release?s=" + u(spaceKey) + "&t=" + u(rootTitle) + "&l=" + u(lang), booklink));
    }

    private void importRelease(String spaceKey, String rootTitle, String lang) {
        String releaseTitle = ctx.formParam("release");
        if (StringService.isNullOrEmpty(releaseTitle)) {
            throw new UserMessage("selectRelease", user);
        } else if (n("alleNochNichtVorhandenen").equals(releaseTitle)) {
            String msg = "Importing release notes: " + spaceKey + " > all non-existing";
            Logger.info(msg);
            user.log(msg);
            service(spaceKey, rootTitle, null, lang).importAllNonExistingReleases();
            ctx.redirect(booklink);
        } else {
            String msg = "Importing release notes: " + spaceKey + " > " + releaseTitle;
        	Logger.info(msg);
        	user.log(msg);
        	String seiteId = service(spaceKey, rootTitle, releaseTitle, lang).importRelease();
        	ctx.redirect(seiteId == null ? booklink : (booklink.replace("/b/", "/s/") + "/" + seiteId));
        }
    }
    
    private ReleaseNotesService service(String spaceKey, String rootTitle, String releaseTitle, String lang) {
        return new ReleaseNotesService(new ReleaseNotesContext(spaceKey, rootTitle, releaseTitle, book, lang));
    }

	@Override
	protected String getPage() {
		return "formular/" + SelectRNCustomerPage.class.getSimpleName();
	}
	
	@Override
	protected String render() {
	    String html = super.render();
	    html = html.replace(" autofocus", " autofocus size=\"20\""); // TODO Amalia
	    return html;
	}
}
