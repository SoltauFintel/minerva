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
public class SelectReleasePage extends BPage {

	@Override
	protected void execute() {
		// TODO MinervaWebapp.factory().gitlabOnlyPage();
		String spaceKey = ctx.queryParam("s");
		String rootTitle = ctx.queryParam("t");
		if (spaceKey == null || rootTitle == null || rootTitle.isEmpty()) {
			throw new RuntimeException("Missing parameters");
		}
		if (isPOST()) {
			importRelease(spaceKey, rootTitle);
		} else {
			displayFormular(spaceKey, rootTitle);
		}
	}

    private void displayFormular(String spaceKey, String rootTitle) {
        ConfluencePage2 rnpage = new ReleaseNotesService().loadReleaseNotesPage(spaceKey, rootTitle);
        if (rnpage == null) {
        	throw new UserMessage("pageDoesntExist", user, s -> s.replace("$t", rootTitle));
        }

        List<String> releases = rnpage.getSubpages().stream().map(i -> i.getTitle()).collect(Collectors.toList());
        releases.add(n("alleNochNichtVorhandenen"));
        // TODO Bereits vorhandene Seiten herausfiltern.

        header(n("loadReleaseNotes"));
        ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
        initColumnFormularGenerator(gen);
        combobox("releases", releases, "", false, model); // TODO Amalia: ID
        TemplatesInitializer.fp.setContent(gen
        		.combobox("release", n("Release"), 5, "releases", true)
                .save(n("Import"))
                .getHTML(booklink + "/rn-select-release?s=" + u(spaceKey) + "&t=" + u(rootTitle), booklink));
    }

    private void importRelease(String spaceKey, String rootTitle) {
        String release = ctx.formParam("release");
        if (StringService.isNullOrEmpty(release)) {
            throw new UserMessage("selectRelease", user);
        } else if (n("alleNochNichtVorhandenen").equals(release)) {
            String msg = "Importing release notes: " + spaceKey + " > all non-existing";
            Logger.info(msg);
            user.log(msg);
            new ReleaseNotesService().importAllNonExistingReleases(spaceKey, rootTitle);
            ctx.redirect(booklink);
        } else {
            String msg = "Importing release notes: " + spaceKey + " > " + release;
        	Logger.info(msg);
        	user.log(msg);
        	new ReleaseNotesService().importRelease(spaceKey, rootTitle, release);
        	ctx.redirect(booklink); // TODO die importierte Release Seite anzeigen
        }
    }

	@Override
	protected String getPage() {
		return "formular/" + SelectRNCustomerPage.class.getSimpleName();
	}
	
	@Override
	protected String render() {
	    String html = super.render();
	    html = html.replace(" autofocus", " autofocus size=\"20\"");
	    return html;
	}
}
