package minerva.releasenotes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.DocField;
import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.JiraCloudAccess.IssueAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;
import github.soltaufintel.amalia.base.IdGenerator;
import minerva.base.NLS;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;

/**
 * Jira cloud
 */
public class ReleaseNotesService2 extends AbstractReleaseNotesService {
    private List<ReleaseNoteTicket> releaseNoteTickets;
    
    public ReleaseNotesService2(ReleaseNotesContext ctx) {
        super(ctx);
    }

    public List<ReleaseTicket> loadReleases(String project) {
        // ctx is null
        return ReleaseTicket.load(jira(), project).stream()
                .filter(rt -> rt.isRelevant())
                .collect(Collectors.toList());
    }

    public void importAllNonExistingReleases() {
        List<ReleaseTicket> releaseTickets = loadReleases(ctx.getProject());
        List<String> existingReleasePageTitles = getExistingReleasePages();
        releaseTickets.removeIf(rt -> existingReleasePageTitles.contains(TITLE_PREFIX + rt.getTargetVersion()));
        if (releaseTickets.isEmpty()) {
            Logger.info("importAllNonExistingReleases: No releases. Nothing to do.");
            return;
        }
        for (ReleaseTicket rt : releaseTickets) {
            ctx.setPageId(rt.getPageId());
            ctx.setReleaseNumber(rt.getTargetVersion());
            importRelease();
        }
    }

    public String importRelease() {
    	Logger.debug("import release for page ID: " + ctx.getPageId());
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
        releasePage.getSeite().getTitle().setString("de", TITLE_PREFIX + releaseNumber);
        releasePage.getSeite().getTitle().setString("en", TITLE_PREFIX + releaseNumber);
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
        return part(html1, project) + part(html2, NLS.get(lang, "generalChanges"));
    }
    
    // Für den gewählten Kunden darf die 'Customer project key' Ticketnummer aus dem "release for" verknüpften Ticket verwendet werden.
    // Ansonsten die RNT Nr..
    private String getCustomerTicketNumber(ReleaseNoteTicket rnt) {
        String ret = null;
        String releaseFor = rnt.getReleaseFor(); // Ticketnr. des über "release for" verknüpfte Ticket
        if (releaseFor != null) {
            List<IssueAccess> list = jira().loadIssues("key=\"" + releaseFor + "\"", i -> i);
            if (list.size() >= 1) {
                IssueAccess ia = list.get(0);
                if (ctx.getProject().equals(ia.text("/fields/project/key"))) { // Project übereinstimmend?
                	ret = ia.text("/fields/customfield_10048"); // Customer project key
                }
            }
        }
    	return ret == null ? rnt.getKey() : ret;
    }
    
    private void getReleasePageContent2(String key, String rnt, DocField rns, DocField rnd, StringBuilder html) {
    	try {
			html.append("<h3>" + key + (rnt == null ? "" : ": " + rnt.trim()) + "</h3>");
			
			if (rns == null) {
			    Logger.info("Release notes import: field RNS does not exist for " + key);
			} else {
			    append(rns, html);
			}
			
			if (rnd == null) {
			    Logger.info("Release notes import: field RND does not exist for " + key);
			} else {
			    append(rnd, html);
			}
		} catch (Exception e) {
			throw new RuntimeException("Error importing release note ticket " + key + ": " + e.getMessage(), e);
		}
    }
    
    private void append(DocField d, StringBuilder html) {
        String text = d.getText();
        if (d.isPlainText()) {
            if ("Blindtext".equalsIgnoreCase(text) || text.isBlank()) {
            	return;
            }
            html.append("<p>");
            html.append(text.trim());
            html.append("</p>");
        } else { // text is HTML
            String prefix = "img/" + ctx.getResultingReleasePage().getSeite().getId() + "/";
            for (Entry<String, byte[]> e : d.getImages().entrySet()) {
                String src = e.getKey();
                String dn = prefix + IdGenerator.createId6() + ".png"; // .png is guessed
                text = text.replace("\"" + src + "\"", "\"" + dn + "\"");
                ctx.getResultingReleasePage().getImages().add(dn);
                saveImage(dn, e.getValue());
            }
            Document doc = Jsoup.parse(text);
			Elements elements = doc.selectXpath("/html/body/p");
			for (int i = elements.size() - 1; i >= 0; i--) {
				Element e = elements.get(i);
				if (e.text().isBlank() || e.text().equals("&nbsp;")) {
					System.out.println("found <p>&nbsp;</p>");
					e.remove();
				} else {
					break;
				}
			}
//            String x = "<p>&nbsp;</p>\n";
//            while (text.endsWith(x)) {
//            	text = text.substring(0, text.length() - x.length());
//            }
			html.append(doc.html());
        }
    }

    private void saveImage(String dn, byte[] data) {
        File imageFile = new File(ctx.getResultingReleasePage().getSeite().filenameImage(dn));
        imageFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private String part(StringBuilder sb, String title) {
        if (sb.toString().isEmpty()) {
            return "";
        }
        return "<h2>" + title + "</h2>" + sb.toString();
    }
    
    private JiraCloudAccess jira() {
    	if (MinervaOptions.JIRA_MAIL.isSet() && MinervaOptions.JIRA_TOKEN.isSet() && MinervaOptions.JIRA_CUSTOMER.isSet()) {
			return new JiraCloudAccess(MinervaOptions.JIRA_MAIL.get(), MinervaOptions.JIRA_TOKEN.get(),
					MinervaOptions.JIRA_CUSTOMER.get());
    	}
    	throw new RuntimeException("Jira Cloud access is not possible because options are not set.");
    }
}
