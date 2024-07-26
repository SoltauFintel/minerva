package minerva.releasenotes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.NlsString;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public abstract class AbstractReleaseNotesService {
    // Wenn Jira-server nicht mehr benutzt wird, kann diese Klasse mit ReleaseNotesService2 vereinigt werden.
    // ReleaseNotesContext muss dann aufgeräumt werden. SelectRNCustomerPage und SelectRNReleasePage können dann weg.
    
    public static final String TITLE_PREFIX = "Release Notes ";
    protected final ReleaseNotesContext ctx;
    
    public AbstractReleaseNotesService(ReleaseNotesContext ctx) {
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

    protected final SeiteSO findCustomerPage() {
        return ctx.getBook().getSeiten()._byTag(tag());
    }

    protected final String tag() {
        return "release-notes-" + ctx.getConfig().getTicketPrefix().toLowerCase();
    }

    protected final void createCustomerPage() {
        SeiteSO customerPage = findCustomerPage();
        if (customerPage == null) {
            customerPage = createSeite(ctx.getBook());
            customerPage.getSeite().getTags().add("release-notes");
            customerPage.getSeite().getTags().add(tag());
            customerPage.getSeite().getTags().add("reversed-order");
            customerPage.getSeite().setSorted(true);
            String customer = ctx.getConfig().getCustomer();
            setTitleAndDummyContent(customerPage, "Programmänderungen " + customer, "Release Notes " + customer);
            customerPage.getSeite().setTocSubpagesLevels(2);
            customerPage.saveMetaTo(ctx.getFiles());
            customerPage.saveHtmlTo(ctx.getFiles(), langs());
        }
        ctx.setCustomerPage(customerPage);
    }

    protected final void setTitleAndDummyContent(SeiteSO seite, String titleDE, String titleEN) {
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
    protected final void createSectionPage(String releaseNumber) {
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
    
    protected final SeiteSO createSeite(SeiteSO parent) {
        return parent.getSeiten().createSeite(parent, parent.getBook(), IdGenerator.createId6());
    }

    protected final void createReleasePages() {
        createCustomerPage();
        String releaseNumber = getReleaseNumber(ctx.getReleasePage() == null ? null : ctx.getReleasePage().getTitle());
        if (releaseNumber.isBlank()) {
            throw new RuntimeException("releaseNumber is empty");
        }
        createSectionPage(releaseNumber);
        SeiteSO seite = createReleasePage(releaseNumber);
        Map<String, String> filenames = ctx.getFiles();
        String prefix = ctx.getResultingReleasePage().getSeite().getBook().getFolder() + "/";
        ctx.getResultingReleasePage().getImages().forEach(dn -> filenames.put(prefix + dn, DirAccess.IMAGE));
        ctx.getBook().dao().saveFiles(filenames,
                new CommitMessage("Release Notes " + ctx.getConfig().getCustomer() + " " + releaseNumber),
                ctx.getBook().getWorkspace());
        Logger.info(releaseNumber +" | Number of saved pages: " + filenames.keySet().stream().filter(i -> i.endsWith(".meta")).count());
        seite.reindex();
    }

    protected String getReleaseNumber(final String pTitle) {
        String title = " " + pTitle + " ";
        // searching for blank + digit[1-9] + dot ...
        int o = title.indexOf(".");
        while (o >= 2) {
            if (title.charAt(o - 2) == ' ' && title.charAt(o - 1) >= '1' && title.charAt(o - 1) <= '9') {
                int oo = title.indexOf(" ", o);
                if (oo > o) {
                    return title.substring(o - 1, oo);
                }
            }
            o = title.indexOf(".", o + 1);
        }
        return ""; // can't extract release number
    }

    protected abstract SeiteSO createReleasePage(String releaseNumber);
    
    protected abstract String getReleasePageContent();
}
