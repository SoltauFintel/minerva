package minerva;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.workspace.WAction;

/**
 * Seite A hat Images, die aus Seite B stammen. So ein Mist!
 */
public class RepairImagesAction extends WAction {
    private List<String> newImageFiles = new ArrayList<>();
    private Map<String, String> files = new HashMap<>();

    @Override
    protected void execute() {
        if (branch.equals("master")) {
            throw new RuntimeException("Action not allowed for master branch!"); // Schutz
        }
        List<String> langs = MinervaWebapp.factory().getLanguages();
        Logger.info("REPAIR IMAGES, branch: " + branch + " | langs: " + langs);
        for (BookSO book : workspace.getBooks()) {
            Logger.info("-- book: " + book.getBook().getFolder());
            for (SeiteSO seite : book.getAlleSeiten()) {
                process(seite, langs);
            }
        }
        Logger.info("images to be saved: " + newImageFiles.size());
        if (!files.isEmpty() || !newImageFiles.isEmpty()) {
            newImageFiles.forEach(dn -> files.put(dn, DirAccess.IMAGE));
            Logger.info("saving " + files.size() + " files ...");
            workspace.dao().saveFiles(files, new CommitMessage("repair images"), workspace);
        }
        Logger.info("end of repair images");
        ctx.redirect("/b/" + branch + "/handbuch");
    }

    private void process(SeiteSO seite, List<String> langs) {
        boolean dirty1 = false;
        for (String lang : langs) {
            boolean dirty2 = false;
            String html = seite.getContent().getString(lang);
            Set<String> imgSrcs = StringService.findHtmlTags(html, "img", "src");
            for (String src : imgSrcs) {
                if (!src.startsWith("img/" + seite.getId() + "/")) { // image from other page?
                    File img = new File(seite.getBook().getFolder() + "/" + src);
                    String dn = seite.getBook().getFolder() + "/img/" + seite.getId() + "/" + img.getName();
                    File target = new File(dn);
                    if (img.isFile() && !target.isFile()) {
                        FileService.copyFile(img, target);
                        newImageFiles.add(dn);
                        html = html.replace("src=\"" + src + "\"", "src=\"img/" + seite.getId() + "/" + img.getName() + "\"");
                        dirty2 = true;
                    } else {
                        Logger.error("Page " + seite.getId() + " \"" + seite.getTitle() + "\" contains illegal image: "
                                + src + " | " + img.toString() + " does " + (img.isFile() ? "" : "NOT ")
                                + "exist. Target file exist: " + target.isFile());
                    }
                }
            }
            if (dirty2) {
                seite.getContent().setString(lang, html);
                dirty1 = true;
            }
        }
        if (dirty1) {
            seite.saveHtmlTo(files, langs);
        }
    }
}
