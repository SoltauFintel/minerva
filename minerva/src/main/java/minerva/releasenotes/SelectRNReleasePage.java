package minerva.releasenotes;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.MinervaWebapp;
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
		if (StringService.isNullOrEmpty(spaceKey)) {
			throw new RuntimeException("Missing parameter");
		}
		ReleaseNotesConfig config = MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs().stream()
		        .filter(c -> c.getSpaceKey().equals(spaceKey)).findFirst().orElse(null);
		if (config == null) {
		    throw new RuntimeException("Unknown space key: " + esc(spaceKey));
		}
		String rootTitle = config.getRootTitle();
		String language = config.getLanguage();
		if (isPOST()) {
			importRelease(config, spaceKey, rootTitle, language);
		} else {
			displayFormular(config, spaceKey, rootTitle, language);
		}
	}

    private void displayFormular(ReleaseNotesConfig config, String spaceKey, String rootTitle, String lang) {
        ConfluencePage2 rnpage = new ReleaseNotesService(null).loadReleaseNotesPage(spaceKey, rootTitle);
        if (rnpage == null) {
        	throw new UserMessage("pageDoesntExist", user, s -> s.replace("$t", rootTitle));
        }

        List<String> releases = rnpage.getSubpages().stream().map(i -> i.getTitle()).collect(Collectors.toList());
        List<String> existingReleasePageTitles = new ReleaseNotesService(new ReleaseNotesContext(config, null, book)).getExistingReleasePages();
        releases.removeIf(title -> existingReleasePageTitles.contains(title));
        releases.add(n("alleNochNichtVorhandenen"));

        header(n("loadReleaseNotes") + " (" + config.getCustomer() + ")");
        ColumnFormularGenerator gen = new ColumnFormularGenerator(1, 1);
        initColumnFormularGenerator(gen);
        combobox("releases", releases, "", false, model); // TODO Amalia: ID
        TemplatesInitializer.fp.setContent(gen
        		.combobox("release", n("Release"), 5, "releases", true)
                .save(n("Import"))
                .getHTML(booklink + "/rn-select-release?s=" + u(config.getSpaceKey()), booklink + "/rn-select-customer"));
    }

    private void importRelease(ReleaseNotesConfig config, String spaceKey, String rootTitle, String lang) {
        String releaseTitle = ctx.formParam("release");
        if (StringService.isNullOrEmpty(releaseTitle)) {
            throw new UserMessage("selectRelease", user);
        } else if (n("alleNochNichtVorhandenen").equals(releaseTitle)) {
            String msg = "Importing release notes: " + spaceKey + " > all non-existing";
            Logger.info(msg);
            user.log(msg);
            service(config, null).importAllNonExistingReleases();
            user.getUser().setPageLanguage(lang);
            ctx.redirect(booklink);
        } else {
            String msg = "Importing release notes: " + spaceKey + " > " + releaseTitle;
        	Logger.info(msg);
        	user.log(msg);
        	String seiteId = service(config, releaseTitle).importRelease();
        	user.getUser().setPageLanguage(lang);
        	ctx.redirect(seiteId == null ? booklink : (booklink.replace("/b/", "/s/") + "/" + seiteId));
        }
    }
    
    private ReleaseNotesService service(ReleaseNotesConfig config, String releaseTitle) {
        return new ReleaseNotesService(new ReleaseNotesContext(config, releaseTitle, book));
    }

	@Override
	protected String getPage() {
		return "formular/" + super.getPage();
	}
	
	@Override
	protected String render() {
	    String html = super.render();
	    html = html.replace(" autofocus", " autofocus size=\"20\""); // TODO Amalia
	    return html;
	}
}
