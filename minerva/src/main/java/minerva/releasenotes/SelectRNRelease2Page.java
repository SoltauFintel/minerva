package minerva.releasenotes;

import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.ReleaseTicket;
import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.book.BPage;

/**
 * Release notes: select release to be imported
 */
public class SelectRNRelease2Page extends BPage {
    private static final String ALL = "!all";
    
    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().getConfig().isDevelopment()) {
            MinervaWebapp.factory().gitlabOnlyPage();
        }
        String ticketPrefix = ctx.queryParam("c");
        if (StringService.isNullOrEmpty(ticketPrefix)) {
            throw new RuntimeException("Missing parameter");
        }
        ReleaseNotesConfig config = ReleaseNotesConfig.get(ticketPrefix);
        if (config == null) {
            throw new RuntimeException("Unknown ticket prefix: " + esc(ticketPrefix));
        }
        String rootTitle = config.getRootTitle();
        String language = config.getLanguage();
        if (isPOST()) {
            importRelease(config, ticketPrefix, rootTitle, language);
        } else {
            displayFormular(config, ticketPrefix, rootTitle, language);
        }
    }

    private void displayFormular(ReleaseNotesConfig config, String project, String rootTitle, String lang) {
        List<ReleaseTicket> releaseTickets = new ReleaseNotesService(null).loadReleases(project);
        List<IdAndLabel> releases = releaseTickets.stream().map(i -> new IdAndLabel() {
                @Override
                public String getId() {
                    return i.getPageId();
                }

                @Override
                public String getLabel() {
                    return AbstractReleaseNotesService.TITLE_PREFIX + i.getTargetVersion();
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
        put("project", esc(project));
        combobox_idAndLabel("releases", releases, "", false);
    }

    private void importRelease(ReleaseNotesConfig config, String project, String rootTitle, String lang) {
        String pageId = ctx.formParam("release"); // It's the Page ID.
        if (StringService.isNullOrEmpty(pageId)) {
            throw new UserMessage("selectRelease", user);
        } else if (ALL.equals(pageId)) {
            String msg = "Importing release notes: " + project + " > all non-existing";
            Logger.info(msg);
            user.log(msg);
            service(config, null, null, project).importAllNonExistingReleases();
            user.getUser().setPageLanguage(lang);
            ctx.redirect(booklink);
        } else {
            String releaseNumber = getReleaseNumber(project, pageId);
            String msg = "Importing release notes: " + project + " > " + releaseNumber + " (" + pageId + ")";
            Logger.info(msg);
            user.log(msg);
            String seiteId = service(config, pageId, releaseNumber, project).importRelease();
            user.getUser().setPageLanguage(lang);
            ctx.redirect(seiteId == null ? booklink : (booklink.replace("/b/", "/s/") + "/" + seiteId));
        }
    }
    
    private String getReleaseNumber(String project, String pageId) {
        // Irgendwie doof, dass ich das nochmal laden muss. Vielleicht das besser über URL-parametern übergeben!?
        List<ReleaseTicket> releaseTickets = new ReleaseNotesService(null).loadReleases(project);
        for (ReleaseTicket rt : releaseTickets) {
            if (rt.getPageId().equals(pageId)) {
                return rt.getTargetVersion();
            }
        }
        throw new RuntimeException("Can't find project/pageId pair in release tickets!");
    }
    
    private ReleaseNotesService service(ReleaseNotesConfig config, String pageId, String releaseNumber, String project) {
        ReleaseNotesContext c = new ReleaseNotesContext(config, pageId, book);
        c.setReleaseNumber(releaseNumber);
        c.setProject(project);
        return new ReleaseNotesService(c);
    }
}
