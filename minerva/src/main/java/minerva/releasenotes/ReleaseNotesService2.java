package minerva.releasenotes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.JiraCloudAccess.IssueAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;
import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.config.MinervaConfig;
import minerva.model.SeiteSO;

/**
 * Jira cloud
 */
public class ReleaseNotesService2 extends AbstractReleaseNotesService {
    private List<ReleaseNoteTicket> releaseNoteTickets;
    
    public ReleaseNotesService2(ReleaseNotesContext ctx) {
        super(ctx);
    }

    public List<ReleaseTicket> loadReleaseNotesPage(String project) {
        // ctx is null
        return ReleaseTicket.load(jira(), project).stream()
                .filter(rt -> rt.isRelevant())
                .collect(Collectors.toList());
    }

    public void importAllNonExistingReleases() {
        // TODO
        throw new RuntimeException("importAllNonExistingReleases() noch nicht implementiert");
    }

    public String importRelease() {
        releaseNoteTickets = ReleaseNoteTicket.load(jira(), ctx.getPageId());
        if (releaseNoteTickets.isEmpty()) {
            Logger.info("Can't import release notes because no 'Release note ticket's were found! Page ID: " + ctx.getPageId());
            return null;
        }
        createReleasePages();
        return ctx.getResultingReleasePage().getId();
    }

    @Override
    protected String getReleaseNumber(final String t) {
        return ctx.getReleaseNumber();
    }

    @Override
    protected SeiteSO createReleasePage(String releaseNumber) {
        SeiteSO parent = ctx.getSectionPage() == null ? ctx.getCustomerPage() : ctx.getSectionPage();
        SeiteSO releasePage = createSeite(parent);
        ctx.setResultingReleasePage(releasePage);
        releasePage.getSeite().getTitle().setString("de", "Release Notes " + releaseNumber);
        releasePage.getSeite().getTitle().setString("en", "Release Notes " + releaseNumber);
        releasePage.getContent().setString(ctx.getLang(), getReleasePageContent());
        releasePage.getSeite().setTocHeadingsLevels(2);
        releasePage.getSeite().getHelpKeys().add(releaseNumber);
        releasePage.saveMetaTo(ctx.getFiles());
        releasePage.saveHtmlTo(ctx.getFiles(), langs());
        parent.getSeiten(ctx.getLang()); // sort
        return releasePage;
    }

    @Override
    protected String getReleasePageContent() {
        String lang = ctx.getConfig().getLanguage();
        String project = ctx.getConfig().getTicketPrefix();
        StringBuilder html1 = new StringBuilder();
        StringBuilder html2 = new StringBuilder();
        for (ReleaseNoteTicket t : releaseNoteTickets) {
            String ctn = getCustomerTicketNumber(t);
            StringBuilder html = ctn.contains(project) ? html1 : html2;
            if ("de".equals(lang)) {
                getReleasePageContent2(ctn, t.getRNT_de(), t.getRNS_de(), t.getRND_de(), t.getRND_de_images(), html);
            } else {
                getReleasePageContent2(ctn, t.getRNT_en(), t.getRNS_en(), t.getRND_en(), t.getRND_en_images(), html);
            }
        }
        String ret = "";
        if (!html1.toString().isEmpty()) {
            ret += "<h2>" + project + "</h2>";
            ret += html1.toString();
        }
        if (!html2.toString().isEmpty()) {
            ret += "<h2>" + NLS.get(lang, "generalChanges") + "</h2>";
            ret += html2.toString();
        }
        return ret;
    }
    
    private String getCustomerTicketNumber(ReleaseNoteTicket rnt) {
        String ret = null;
        String releaseFor = rnt.getReleaseFor();
        if (releaseFor != null) {
            List<IssueAccess> list = jira().loadIssues("key=\"" + releaseFor + "\"", i -> i);
            if (list.size() >= 1) {
                IssueAccess ia = list.get(0);
                ret = ia.text("/fields/customfield_10059");
            }
        }
        return ret == null ? rnt.getKey() : ret;
    }
    
    private void getReleasePageContent2(String key, String rnt, String rns, String rnd, Map<String, byte[]> images,
            StringBuilder html) {
        html.append("<h3>" + key + ": " + rnt + "</h3>");
        if (!"Blindtext".equals(rns)) {
            html.append("<p>" + rns + "</p>");
        }
        if (!"Blindtext".equals(rnd)) {
            html.append(rnd);
        }
    }
    
    private JiraCloudAccess jira() {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        return new JiraCloudAccess(c.getJiraMail(), c.getJiraToken(), c.getJiraCustomer());
    }
}
