package minerva.releasenotes;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.book.BPage;
import minerva.confluence.ConfluencePage2;

/**
 * Select release to be imported
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
			String release = ctx.formParam("release");
			Logger.info("Importing release notes: " + spaceKey + " > " + release);
			// TODO serverlog
			new ReleaseNotesService().importRelease(spaceKey, rootTitle, release);
			ctx.redirect(booklink); // TODO die importierte Release Seite anzeigen
		} else {
			ConfluencePage2 rnpage = new ReleaseNotesService().loadReleaseNotesPage(spaceKey, rootTitle);
			if (rnpage == null) {
				throw new RuntimeException("Page with title \"" + rootTitle + "\" does not exist!"); // TODO NLS UserMsg
			}

			List<String> releases = rnpage.getSubpages().stream().map(i -> i.getTitle()).collect(Collectors.toList());
			// TODO hier fehlt noch der zusätzliche Eintrag "alle noch nicht vorhandene"
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
	}

	@Override
	protected String getPage() {
		return "formular/" + SelectRNCustomerPage.class.getSimpleName();
	}
}
