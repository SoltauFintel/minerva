package minerva;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
        Logger.info("==== REPAIR IMAGES v2.0 ====  branch: " + branch + " | langs: " + langs);
        for (BookSO book : workspace.getBooks()) {
            Logger.info("-- book: " + book.getBook().getFolder());
            for (SeiteSO seite : book.getAlleSeiten()) {
                process(seite, langs);
            }
        }
        Logger.info("STAT: images to be saved: " + newImageFiles.size() + " | html files: " + files.size());
        if (!files.isEmpty() || !newImageFiles.isEmpty()) {
            newImageFiles.forEach(dn -> files.put(dn, DirAccess.IMAGE));
            Logger.info("saving " + files.size() + " files ...");
            workspace.dao().saveFiles(files, new CommitMessage("repair images"), workspace);
        }
        Logger.info("---- end of repair images ----");
        ctx.redirect("/b/" + branch + "/handbuch");
    }

    private void process(SeiteSO seite, List<String> langs) {

        // neuer Algorithmus

        List<String> dirty = new ArrayList<>();
        Set<String> imgSrcs = new TreeSet<>();
        for (String lang : langs) {
            String html = seite.getContent().getString(lang);
            imgSrcs.addAll(StringService.findHtmlTags(html, "img", "src"));
        }
        for (String src : imgSrcs) {
            if (!src.startsWith("img/" + seite.getId() + "/")) { // image from other page?
                Logger.debug(seite.getBook().getBook().getFolder() + " " + seite.getId()
                        + " | image is from other page: " + src);

                // Kann es repariert werden?
                File vorhandeneGrafik = new File(seite.getBook().getFolder() + "/" + src);
                String dnt = vorhandeneGrafik.getName();
                String dn = seite.getBook().getFolder() + "/img/" + seite.getId() + "/" + dnt;
                File target = new File(dn);

                if (vorhandeneGrafik.isFile() && !target.isFile()) {
                    // Ja, es kann repariert werden.
                    FileService.copyFile(vorhandeneGrafik, target.getParentFile());
                    newImageFiles.add(dn);
                    for (String lang : langs) {
                        String html = seite.getContent().getString(lang);
                        String x = "src=\"" + src + "\"";
                        if (html.contains(x)) {
                            html = html.replace(x, "src=\"img/" + seite.getId() + "/" + dnt + "\"");
                            seite.getContent().setString(lang, html);
                            dirty.add(lang);
                        }
                    }

                } else {
                    Logger.debug("-- Fehler: kann nicht repariert werden. vorhandene Grafik: "
                            + vorhandeneGrafik.isFile() + ", Zieldatei: " + target.isFile());
                }
            }
        }
        for (String lang : dirty) {
            files.put(seite.filenameHtml(lang), seite.getContent().getString(lang));
        }

        // alter Algorithmus

//        boolean dirty1 = false;
//        for (String lang : langs) {
//            boolean dirty2 = false;
//            String html = seite.getContent().getString(lang);
//            Set<String> imgSrcs = StringService.findHtmlTags(html, "img", "src");
//            for (String src : imgSrcs) {
//                if (!src.startsWith("img/" + seite.getId() + "/")) { // image from other page?
//                    File img = new File(seite.getBook().getFolder() + "/" + src);
//
//                    Logger.debug(seite.getBook().getBook().getFolder() + " " + seite.getId()
//                        + " | image from other page: " + src + " | img.name=" + img.getName());
//                    
//                    String dn = seite.getBook().getFolder() + "/img/" + seite.getId() + "/" + img.getName();
//                    
//                    File target = new File(dn);
//                    
//                    if (img.isFile() && !target.isFile()) {
//                        FileService.copyFile(img, target.getParentFile());
//                        newImageFiles.add(dn);
//                        html = html.replace("src=\"" + src + "\"", "src=\"img/" + seite.getId() + "/" + img.getName() + "\"");
//                        dirty2 = true;
//                    } else if (img.isFile() && target.isFile() && img.length() == target.length()) {
//                        html = html.replace("src=\"" + src + "\"", "src=\"img/" + seite.getId() + "/" + img.getName() + "\"");
//                        dirty2 = true;
//                    } else {
//                        Logger.error("Page " + seite.getId() + " \"" + seite.getTitle() + "\" contains illegal image: "
//                                + src + " | " + img.toString() + " does " + (img.isFile() ? "" : "NOT ")
//                                + "exist. Target file exist: " + target.isFile() + ", imgSize=" + img.length() + " <> targetSize=" + target.length());
//                    }
//                }
//            }
//            if (dirty2) {
//                seite.getContent().setString(lang, html);
//                dirty1 = true;
//            }
//        }
//        if (dirty1) {
//            seite.saveHtmlTo(files, langs);
//        }
    }
}
