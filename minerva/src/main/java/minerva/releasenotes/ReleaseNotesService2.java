package minerva.releasenotes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.DocField;
import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.JiraCloudAccess.IssueAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;
import github.soltaufintel.amalia.base.IdGenerator;
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
            getReleasePageContent2(ctn, t.getRNT(lang), t.getRNS().get(lang), t.getRND().get(lang), html);
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
    
    private void getReleasePageContent2(String key, String rnt, DocField rns, DocField rnd, StringBuilder html) {
        html.append("<h3>" + key + ": " + rnt.trim() + "</h3>");
        append(rns, html);
        append(rnd, html);
    }
    
    private void append(DocField d, StringBuilder html) {
        String text = d.getText();
        if (d.isPlainText()) {
            if (!"Blindtext".equals(text)) {
                html.append("<p>");
                html.append(text.trim());
                html.append("</p>");
            }
        } else { // text is HTML
            SeiteSO seite = ctx.getResultingReleasePage();
            String seiteId = seite.getId();
            for (Entry<String, byte[]> e : d.getImages().entrySet()) {
                String src = e.getKey();
                String dn = "img/" + seiteId + "/" + IdGenerator.createId6() + ".png"; // .png is guessed
                text = text.replace("\"" + src + "\"", "\"" + dn + "\"");
                seite.getImages().add(dn);
                saveImage(dn, e.getValue());
            }
            html.append(text);
        }
    }

    private void saveImage(String dn, byte[] data) {
        File imageFile = new File(ctx.getResultingReleasePage().filenameImage(dn));
        imageFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private JiraCloudAccess jira() {
        MinervaConfig c = MinervaWebapp.factory().getConfig();
        return new JiraCloudAccess(c.getJiraMail(), c.getJiraToken(), c.getJiraCustomer());
    }
}
