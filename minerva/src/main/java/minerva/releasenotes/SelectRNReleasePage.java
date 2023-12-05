package minerva.releasenotes;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.book.BPage;
import minerva.confluence.ConfluencePage2;

/**
 * Release notes: select release to be imported
 */
public class SelectRNReleasePage extends BPage {
    private static final String ALL = "!all";
    
	@Override
	protected void execute() {
        if (!MinervaWebapp.factory().getConfig().isDevelopment()) {
            MinervaWebapp.factory().gitlabOnlyPage();
        }
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

        List<IdAndLabel> releases = rnpage.getSubpages().stream().map(i -> new IdAndLabel() {
                @Override
                public String getId() {
                    return i.getId();
                }

                @Override
                public String getLabel() {
                    return i.getTitle();
                }
            }).collect(Collectors.toList());
        List<String> existingReleasePageTitles = new ReleaseNotesService(new ReleaseNotesContext(config, null, book)).getExistingReleasePages();
        releases.removeIf(title -> existingReleasePageTitles.contains(title.getLabel()));
        releases.add(new IdAndLabel() {
            @Override
            public String getId() {
                return ALL;
            }

            @Override
            public String getLabel() {
                return n("alleNochNichtVorhandenen");
            }
        });

        header(n("loadReleaseNotes") + " (" + config.getCustomer() + ")");
        put("spaceKey", esc(spaceKey));
        combobox_idAndLabel("releases", releases, "", false);
    }

    private void importRelease(ReleaseNotesConfig config, String spaceKey, String rootTitle, String lang) {
        String release = ctx.formParam("release"); // It's the ID.
        if (StringService.isNullOrEmpty(release)) {
            throw new UserMessage("selectRelease", user);
        } else if (ALL.equals(release)) {
            String msg = "Importing release notes: " + spaceKey + " > all non-existing";
            Logger.info(msg);
            user.log(msg);
            service(config, null).importAllNonExistingReleases();
            user.getUser().setPageLanguage(lang);
            ctx.redirect(booklink);
        } else {
            String msg = "Importing release notes: " + spaceKey + " > " + release;
        	Logger.info(msg);
        	user.log(msg);
        	String seiteId = service(config, release).importRelease();
        	user.getUser().setPageLanguage(lang);
        	ctx.redirect(seiteId == null ? booklink : (booklink.replace("/b/", "/s/") + "/" + seiteId));
        }
    }
    
    private ReleaseNotesService service(ReleaseNotesConfig config, String releaseId) {
        return new ReleaseNotesService(new ReleaseNotesContext(config, releaseId, book));
    }
}
