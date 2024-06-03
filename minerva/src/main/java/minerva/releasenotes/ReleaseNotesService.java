package minerva.releasenotes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.NLS;
import minerva.config.MinervaFactory;
import minerva.confluence.ConfluenceAccess;
import minerva.confluence.ConfluencePage2;
import minerva.model.SeiteSO;
import minerva.validate.ValidatorService;

/**
 * Jira server
 */
@Deprecated
public class ReleaseNotesService extends AbstractReleaseNotesService {
    private ConfluenceAccess access;
    
    public ReleaseNotesService(ReleaseNotesContext ctx) {
        super(ctx);
    }
    
    public ConfluencePage2 loadReleaseNotesPage(String spaceKey, String rootTitle) {
        // ctx is null
        MinervaFactory fac = MinervaWebapp.factory();
        File imagesFolder = fac.getWorkFolder("release-notes-images");
        String baseUrl = fac.getConfig().getReleaseNotesBaseUrl();
        String token = fac.getConfig().getReleaseNotesToken();
        access = new ConfluenceAccess(baseUrl, token, spaceKey, imagesFolder, baseUrl);
        List<ConfluencePage2> pages = access.searchPages();
        return access.byTitle(rootTitle, pages); // The sub-pages are the Release pages, the sub-sub-pages are the ticket pages.
    }

    public void importAllNonExistingReleases() {
        ConfluencePage2 root = loadReleaseNotesPage(ctx.getSpaceKey(), ctx.getRootTitle());
        List<String> existing = getExistingReleasePages();
        for (ConfluencePage2 release : root.getSubpages()) {
            if (!release.getSubpages().isEmpty() && !existing.contains(release.getTitle())) {
                importRelease2(release);
            }
        }
    }

    public String importRelease() {
        ConfluencePage2 root = loadReleaseNotesPage(ctx.getSpaceKey(), ctx.getRootTitle());
        ConfluencePage2 release = root.getSubpages().stream().filter(i -> i.getId().equals(ctx.getReleaseId())).findFirst().orElse(null);
        if (release == null) {
            Logger.info("Can't import release notes because release page is not found. Release ID: " + ctx.getReleaseId());
            return null;
        } else if (release.getSubpages().isEmpty()) {
            Logger.info(release.getTitle() + " | No import because there are no ticket pages.");
            return null;
        }
        return importRelease2(release);
    }

    private String importRelease2(ConfluencePage2 release) {
        Logger.info("importing releaste notes... | " + release.getTitle());
        ctx.setReleasePage(release);
        for (ConfluencePage2 sub : release.getSubpages()) {
            access.loadPage(sub);
            for (ConfluencePage2 details : sub.getSubpages()) {
                access.loadPage(details);
            }
        }
        createReleasePages();
        return ctx.getResultingReleasePage().getId();
    }

    @Override
    protected SeiteSO createReleasePage(String releaseNumber) {
        SeiteSO parent = ctx.getSectionPage() == null ? ctx.getCustomerPage() : ctx.getSectionPage();
        SeiteSO releasePage = createSeite(parent);
        ctx.setResultingReleasePage(releasePage);
        releasePage.getSeite().getTitle().setString("de", ctx.getReleasePage().getTitle());
        releasePage.getSeite().getTitle().setString("en", ctx.getReleasePage().getTitle());
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
        List<String> part1 = new ArrayList<>();
        List<String> part2 = new ArrayList<>();
        ValidatorService v = new ValidatorService();
        for (ConfluencePage2 src : ctx.getReleasePage().getSubpages()) {
            String body = v.removeEmptyLinesAtEnd(src.getHtml());
            for (ConfluencePage2 details : src.getSubpages()) {
                body += v.removeEmptyLinesAtEnd(details.getHtml());
            }
            String html = "<h3>" + src.getTitle() + "</h3>"
                    + processImages(body, ctx.getResultingReleasePage());
            if (src.getTitle().contains(ctx.getConfig().getTicketPrefix())) { // customer-specific
                part1.add(html);
            } else { // general
                part2.add(html);
            }
        }
        return part2html(part1, ctx.getConfig().getCustomer())
                + part2html(part2, NLS.get(ctx.getLang(), "generalChanges"));
    }

    private String processImages(String html, SeiteSO seite) {
        Set<String> images = ConfluenceAccess.extract(html, "img", "src");
        File imagesFolder = MinervaWebapp.factory().getWorkFolder("release-notes-images");
        for (String img : images) {
            if (img.startsWith("img/")) {
                File imgFile = new File(imagesFolder, img.substring("img/".length()));
                if (imgFile.isFile()) {
                    File targetFolder = new File(ctx.getBook().getFolder(), "img/" + seite.getId());
                    FileService.copyFile(imgFile, targetFolder);
                    imgFile.delete();
                    html = html.replace(img, "img/" + seite.getId() + "/" + imgFile.getName());
                    ctx.getFiles().put(seite.filenameImage("img/" + seite.getId() + "/" + imgFile.getName()),
                            DirAccess.IMAGE);
                } else {
                    throw new RuntimeException("File not found: " + imgFile.getAbsolutePath() + "\nin HTML: " + img);
                }
            }
        }
        return html;
    }

    private String part2html(List<String> part, String title) {
        return (part.isEmpty() ? "" : ("<h2>" + title + "</h2>"))
                + part.stream().sorted().collect(Collectors.joining());
    }
}
