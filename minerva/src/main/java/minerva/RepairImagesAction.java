package minerva;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.workspace.WAction;

/**
 * Seite A hat Images, die aus Seite B stammen. So ein Mist!
 */
public class RepairImagesAction extends WAction {

    @Override
    protected void execute() {
        List<String> langs = MinervaWebapp.factory().getLanguages();
        Logger.info("REPAIR IMAGES, branch: " + branch + " | langs: " + langs);
        for (BookSO book : workspace.getBooks()) {
            Logger.info("-- book: " + book.getBook().getFolder());
            for (SeiteSO seite : book.getAlleSeiten()) {
                process(seite, langs);
            }
        }
        Logger.info("end of repair images");
        ctx.redirect("/b/" + branch + "/handbuch");
    }

    private void process(SeiteSO seite, List<String> langs) {
        for (String lang : langs) {
            String html = seite.getContent().getString(lang);
            Set<String> imgSrcs = StringService.findHtmlTags(html, "img", "src");
            for (String src : imgSrcs) {
                if (!src.startsWith("img/" + seite.getId() + "/")) { // image from other page?
                    File img = new File(seite.getBook().getFolder() + "/" + src);
                    Logger.error("Page " + seite.getId() + " \"" + seite.getTitle() + "\" contains illegal image: "
                            + src + " | " + img.toString() + " does " + (img.isFile() ? "" : "NOT ") + "exist.");
                }
            }
        }
    }
}
