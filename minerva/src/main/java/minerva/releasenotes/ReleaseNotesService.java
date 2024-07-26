package minerva.releasenotes;

import static de.xmap.jiracloud.ReleaseNoteTicket.makeSortKey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import de.xmap.jiracloud.DocField;
import de.xmap.jiracloud.JiraCloudAccess;
import de.xmap.jiracloud.ReleaseNoteTicket;
import de.xmap.jiracloud.ReleaseTicket;
import github.soltaufintel.amalia.base.IdGenerator;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.NLS;
import minerva.base.NlsString;
import minerva.base.UserMessage;
import minerva.config.MinervaOptions;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

/**
 * Release Notes service for Jira cloud
 */
public class ReleaseNotesService {
    public static final String TITLE_PREFIX = "Release Notes ";
    protected final ReleaseNotesContext ctx;
    private List<ReleaseNoteTicket> releaseNoteTickets;
    
    public ReleaseNotesService(ReleaseNotesContext ctx) {
        this.ctx = ctx;
    }

    public List<String> getExistingReleasePages() {
        List<String> titles = new ArrayList<>();
        SeiteSO customerPage = findCustomerPage();
        if (customerPage != null) {
            for (SeiteSO sectionPage : customerPage.getSeiten()) {
                if (sectionPage.getSeiten().isEmpty()) { // If no subpages it's a release page.
                    if (!sectionPage.getSeite().getTags().contains("rnignore")) {
                        titles.add(sectionPage.getSeite().getTitle().getString(ctx.getLang()));
                    }
                } else { // Subpages are release pages.
                    sectionPage.getSeiten().forEach(seite -> {
                        if (!seite.getSeite().getTags().contains("rnignore")) {
                            titles.add(seite.getSeite().getTitle().getString(ctx.getLang()));
                        }
                    });
                }
            }
        }
        return titles;
    }

    public String getExistingReleasePages_getSeiteId(String x) {
        SeiteSO customerPage = findCustomerPage();
        if (customerPage != null) {
            for (SeiteSO sectionPage : customerPage.getSeiten()) {
                if (sectionPage.getSeiten().isEmpty()) { // If no subpages it's a release page.
                    String title = sectionPage.getSeite().getTitle().getString(ctx.getLang());
                    if (title.equals(x)) {
                        return sectionPage.getSeite().getId();
                    }
                } else { // Subpages are release pages.
                    for (SeiteSO seite : sectionPage.getSeiten()) {
                        String title = seite.getSeite().getTitle().getString(ctx.getLang());
                        if (title.equals(x)) {
                            return seite.getId();
                        }
                    }
                }
            }
        }
        return null;
    }

    private SeiteSO findCustomerPage() {
        return ctx.getBook().getSeiten()._byTag(tag());
    }

    private String tag() {
        return "release-notes-" + ctx.getConfig().getTicketPrefix().toLowerCase();
    }

    private void createCustomerPage() {
        SeiteSO customerPage = findCustomerPage();
        if (customerPage == null) {
            customerPage = createSeite(ctx.getBook());
            customerPage.getSeite().getTags().add("release-notes");
            customerPage.getSeite().getTags().add(tag());
            customerPage.getSeite().getTags().add("reversed-order");
            customerPage.getSeite().setSorted(true);
            String customer = ctx.getConfig().getTicketPrefix();
            setTitleAndDummyContent(customerPage, "Programmänderungen " + customer, "Release Notes " + customer);
            customerPage.getSeite().setTocSubpagesLevels(2);
            customerPage.saveMetaTo(ctx.getFiles());
            customerPage.saveHtmlTo(ctx.getFiles(), langs());
        }
        ctx.setCustomerPage(customerPage);
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

    private String section(String releaseNumber) {
        int o = releaseNumber.lastIndexOf(".");
        return o >= 0 ? (releaseNumber.substring(0, o + 1) + "x") : null;
    }

    private SeiteSO findSectionPage(String title) {
        return title == null ? null : ctx.getCustomerPage().getSeiten(ctx.getLang())._byTitle(title, ctx.getLang());
    }

    private SeiteSO createSeite(BookSO parent) {
        return parent.getSeiten().createSeite(parent.getISeite(), parent, IdGenerator.createId6());
    }
    
    private SeiteSO createSeite(SeiteSO parent) {
        return parent.getSeiten().createSeite(parent, parent.getBook(), IdGenerator.createId6());
    }

    private void createReleasePages() {
        createCustomerPage();
        String releaseNumber = ctx.getReleaseNumber();
        if (releaseNumber.isBlank()) {
            throw new RuntimeException("releaseNumber is empty");
        }
        createSectionPage(releaseNumber);
        SeiteSO seite = createReleasePage(releaseNumber);
        Map<String, String> filenames = ctx.getFiles();
        String prefix = ctx.getResultingReleasePage().getSeite().getBook().getFolder() + "/";
        ctx.getResultingReleasePage().getImages().forEach(dn -> filenames.put(prefix + dn, DirAccess.IMAGE));
        ctx.getBook().dao().saveFiles(filenames,
                new CommitMessage("Release Notes " + ctx.getConfig().getTicketPrefix() + " " + releaseNumber),
                ctx.getBook().getWorkspace());
        Logger.info(releaseNumber +" | Number of saved pages: " + filenames.keySet().stream().filter(i -> i.endsWith(".meta")).count());
        seite.reindex();
    }

    private SeiteSO createReleasePage(String releaseNumber) {
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

    private String getReleasePageContent() {
        String lang = ctx.getConfig().getLanguage();
        String project = ctx.getConfig().getTicketPrefix();
        StringBuilder html1 = new StringBuilder();
        StringBuilder html2 = new StringBuilder();
        JiraCloudAccess jira = jira();
        for (ReleaseNoteTicket t : releaseNoteTickets) {
            t.loadCustomerTicketNumberAndType(ctx.getProject(), jira);
            if ("Bug".equalsIgnoreCase(t.getReleaseFor_issueType())) {
                t.setSort("2" + makeSortKey(t.getCustomerTicketNumber()));
            } else {
                t.setSort("1" + makeSortKey(t.getCustomerTicketNumber()));
            }
        }
        releaseNoteTickets.sort((a, b) -> a.getSort().compareTo(b.getSort()));
        for (ReleaseNoteTicket t : releaseNoteTickets) {
            String ctn = t.getCustomerTicketNumber();
            Logger.debug("Release note ticket " + t.getKey() + ": " + t.getReleaseFor() + ", "
                    + t.getReleaseFor_issueType() + ", sort=" + t.getSort() + ", " + ctn);
            StringBuilder html = ctn.contains(project) ? html1 : html2;
            getReleasePageContent2(ctn, t.getRNT(lang), t.getRNS().get(lang), t.getRND().get(lang), html);
        }
        return part(html1, project) + part(html2, NLS.get(lang, "generalChanges"));
    }

    public List<ReleaseTicket> loadReleases(String project) {
        // ctx is null
        return ReleaseTicket.load(jira(), project).stream()
                .filter(rt -> rt.isRelevant())
                .collect(Collectors.toList());
    }
    
    public List<ReleaseTicket> loadReleases_raw(String project) {
        // ctx can be null
    	return ReleaseTicket.load(jira(), project);
    }

    public List<ReleaseTicket> loadAllReleases_raw() {
        // ctx can be null
    	return ReleaseTicket.load(jira(), null);
    }

    public void importAllNonExistingReleases() {
        List<ReleaseTicket> releaseTickets = loadReleases(ctx.getProject());
        List<String> existingReleasePageTitles = getExistingReleasePages();
        releaseTickets.removeIf(rt -> existingReleasePageTitles.contains(TITLE_PREFIX + rt.getTargetVersion())); // TODO funktioniert das für de?
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

    // also important method: getReleasePageContent
    public String importRelease() {
    	Logger.debug("import release for page ID: " + ctx.getPageId());
        JiraCloudAccess jira = jira();
        releaseNoteTickets = ReleaseNoteTicket.load(jira, ctx.getPageId());
        if (releaseNoteTickets.isEmpty()) {
            Logger.info("Can't import release notes because no Release note tickets were found! Page ID: " + ctx.getPageId());
            return null;
        }
        createReleasePages();
        return ctx.getResultingReleasePage().getId();
    }
    
    public List<ReleaseNoteTicket> loadReleaseNoteTickets(String pageId) {
    	return ReleaseNoteTicket.load(jira(), pageId);
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
            Elements elements = doc.selectXpath("/html/body/*"); // Leerzeilen am Ende killen
            for (int i = elements.size() - 1; i >= 0; i--) {
                Element e = elements.get(i);
                if ("p".equalsIgnoreCase(e.tagName()) && e.childrenSize() == 0 && e.text().isBlank()) {
                    e.remove();
                } else {
                    break;
                }
            }
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

    public static String reimport(SeiteSO seite, List<String> langs) {
        // check if allowed
        if (!seite.isReleaseNotesReimportAllowed(langs)) {
            throw new RuntimeException("Action not possible!");
        }
        UserSO user = seite.getBook().getWorkspace().getUser();
        if (seite.getSeite().getHelpKeys().size() != 1) {
            throw new UserMessage("pageOnly1HelpKey", user);
        }
        String releaseNumber = seite.getSeite().getHelpKeys().get(0);
        if (!(releaseNumber.contains(".") && releaseNumber.charAt(0) >= '0' && releaseNumber.charAt(0)<='9') ){
            throw new UserMessage("helpKeyMustBeReleaseNumber", user);
        }
        
        SeiteSO customerPage = seite.getParent().getParent();
        String customer = getCustomer(customerPage, user);
        ReleaseNotesConfig config = ReleaseNotesConfig.get(customer);
        if (config == null) {
            Logger.error("ReleaseNotesConfig is null. customer=" + customer);
            throw new UserMessage("unknownCustomerCode", user);
        }
        ReleaseNotesContext context = new ReleaseNotesContext(config, null, seite.getBook());
        
        return new ReleaseNotesService(context).reimport(customer, releaseNumber, seite);
    }
    
    private static String getCustomer(SeiteSO customerPage, UserSO user) {
        for (String tag : customerPage.getSeite().getTags()) {
            final String x = "release-notes-";
            if (tag.startsWith(x)) {
                String customer = tag.substring(x.length()).toUpperCase();
                if (!customer.isBlank()) {
                    return customer; // = ticket prefix
                }
            }
        }
        throw new UserMessage("cantGetCustomer", user);
    }

    private String reimport(String project, String releaseNumber, SeiteSO seite) {
        WorkspaceSO w = seite.getBook().getWorkspace();
        String pageId = loadPageId(project, releaseNumber, w);
        
        // Step 1: reimport
        String msg = "Re-importing release notes: " + project + " " + releaseNumber + " | " + pageId + " | " + w.getBranch();
        Logger.info(msg);
        seite.log(msg);
        ctx.setPageId(pageId);
        ctx.setReleaseNumber(releaseNumber);
        ctx.setProject(project);
        String seiteId = importRelease();
        if (seiteId == null) {
            throw new UserMessage("reimportFailed1", w, m -> m.replace("$p", pageId));
        }
        seite.getBook().getWorkspace().getUser().getUser().setPageLanguage(ctx.getLang());
        
        // Step 2: delete old page
        Logger.info("delete old release notes page: " + seite.getId());
        seite.remove();
        
        Logger.info("Re-import " + project + " " + releaseNumber + " successful. | new page: " + seiteId);
        return seiteId;
    }
    
    private String loadPageId(String project, String releaseNumber, WorkspaceSO w) {
        String pageId = null;
        List<ReleaseTicket> releaseTickets = loadReleases(project);
        for (ReleaseTicket rt : releaseTickets) {
            if (releaseNumber.equals(rt.getTargetVersion())) {
                if (pageId == null) {
                    pageId = rt.getPageId();
                } else if (!pageId.equals(rt.getPageId())) {
                    throw new UserMessage("multipleTargetVersion", w, msg -> msg.replace("$rn", releaseNumber));
                }
            }
        }
        if (pageId == null) {
            throw new UserMessage("noReleaseTicket", w, msg -> msg.replace("$rn", releaseNumber).replace("$c", project));
        }        
        return pageId;
    }
}
