package minerva.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Seite;

public class ConfluenceToMinervaMigrationService {
    private final File sourceFolder;
    private final WorkspaceSO workspace;
    private final List<String> langs;
    private Map<String, String> paerchen;
    private ConfluencePage root_de;
    private ConfluencePage root_en;
    public int gef = 0, gesamt = 0;
    private File htmlSourceFolder;

    public ConfluenceToMinervaMigrationService(File sourceFolder, WorkspaceSO workspace, List<String> langs) {
        this.sourceFolder = sourceFolder;
        this.workspace = workspace;
        this.langs = langs;
    }

    public void migrate() throws Exception {
        Logger.info("[Migration] Start of migration | source folder: " + sourceFolder.getAbsolutePath());

        // Schritt 1: pullen, alle Dateien löschen und pushen
        workspace.pull();
        deleteWorkspace();
        workspace.pull(); // nochmal, um Objektstruktur neu aufzubauen
        Logger.info("Migration init phase completed");

        // Schritt 2: Confluence Daten laden
        readMappings(new File(sourceFolder, "mappings2"));
        htmlSourceFolder = new File(sourceFolder, "html");
        readHtmlFiles(htmlSourceFolder);

        // Schritt 3: Inhalte übertragen
        Logger.info("Migration main part starts");
        int position = 1;
        for (ConfluencePage sp : root_de.getSubpages()) {
            migrateBook(sp, position++);
        }
        Logger.info("Migration fertig");
    }

    private void deleteWorkspace() {
        File[] files = new File(workspace.getFolder()).listFiles();
        if (files == null) {
            throw new RuntimeException("files is null");
        }
        Set<String> filenames = new HashSet<>();
        String x = workspace.getFolder() + "/";
        for (File file : files) {
            if (file.getName().startsWith(".")) {
            } else if (file.isDirectory()) {
                FileService.deleteFolder(file);
                filenames.add(file.getAbsolutePath().replace("\\", "/").replace(x, ""));
            } else {
                file.delete();
                filenames.add(file.getAbsolutePath().replace("\\", "/").replace(x, ""));
            }
        }
        if (filenames.isEmpty()) {
            Logger.info("[Migration] deleteWorkspace() muss nichts machen");
        } else {
            MinervaWebapp.factory().getGitlabRepository().push("(Migration) empty branch " + workspace.getBranch(),
                    workspace, new HashSet<>(), filenames, () -> {
                    });
            Logger.info("[Migration] deleteWorkspace() ok");
        }
    }

    private void readHtmlFiles(File confluenceHtmlDir) {
        Logger.info("readHtmlFiles: " + confluenceHtmlDir.getAbsolutePath());
        ConfluencePage root = FileService.loadJsonFile(new File(confluenceHtmlDir, "data.json"), ConfluencePage.class);
        root_de = root.getSubpages().get(0);
        root_en = root.getSubpages().get(1);
    }

    private void readMappings(File mappingsDir) throws IOException {
        Logger.info("readMappings: " + mappingsDir.getAbsolutePath());
        paerchen = new HashMap<>();
        File[] files = mappingsDir.listFiles();
        if (files == null) {
            throw new RuntimeException("files is null");
        }
        final String ticketSystemUrl = System.getenv("MINERVA_TICKETSYSTEMURL");
        if (StringService.isNullOrEmpty(ticketSystemUrl)) {
            throw new RuntimeException("Env var MINERVA_TICKETSYSTEMURL is not set!");
        }
        final String xde = "de: " + ticketSystemUrl;
        final String xen = "en: " + ticketSystemUrl;

        for (File file : files) {
            String content = new String(Files.readAllBytes(file.toPath())).replace("\r\n", "\n");
            String de = "", en = "";
            for (String line : content.split("\n")) {
                if (line.trim().isBlank()) {
                } else if (line.startsWith("de:")) {
                    if (line.startsWith(xde)) {
                        de = line.substring(xde.length()).trim();
                    }
                } else if (line.startsWith("en:")) {
                    if (line.startsWith(xen)) {
                        en = line.substring(xen.length()).trim();
                    }
                } else { // GO Key
                    if (!de.isEmpty() || !en.isEmpty()) {
                        if (!(en.isBlank() && !StringService.isNullOrEmpty(paerchen.get(de)))) {
                            paerchen.put(de, en);
                        }
                    }
                    de = "";
                    en = "";
                }
            }
        }
        Logger.info("Pärchen: " + paerchen.size());
    }

    private void migrateBook(ConfluencePage sp, int position) {
        Map<String, String> files = new HashMap<>();

        // create book ----
        String folder = getFolder(sp.getTitle());
        Logger.info("migrate book: " + sp.getTitle() + " -> folder: " + folder);
        NlsString titles = new NlsString();
        titles.setString("de", sp.getTitle());
        String title_en = sp.getTitle();
        ConfluencePage en = findEnglishPage(sp);
        if (en != null) {
            title_en = en.getTitle();
        } else {
            if ("Prozessbeschreibung".equals(title_en)) {
                title_en = "Process descriptions";
            } else if ("Tipps und Tricks".equals(title_en)) {
                title_en = "FAQ";
            } else if ("Betriebshandbuch".equals(title_en)) {
                title_en = "Operating manual";
            } else if ("Programmänderungen".equals(title_en)) {
                title_en = "Release notes";
            }
        }
        titles.setString("en", title_en);
        workspace.getBooks().createBook(folder, titles, langs, position);
        BookSO book = workspace.getBooks().byFolder(folder);

        // migrate first page ----
        /*SeiteSO seite1 = book.getSeiten().createSeite(book.getISeite(), book, sp.getId());
        migratePage(sp, seite1, files);*/
        for (ConfluencePage sub : sp.getSubpages()) {
            SeiteSO subTp = book.getSeiten().createSeite(book.getISeite(), book, sub.getId());
            migratePage(sub, subTp, files);
        }

        // commit and push everything ----
        Logger.info("saving " + files.size() + " files for book \"" + sp.getTitle() + "\"...");
        if (!files.isEmpty()) {
            workspace.dao().saveFiles(files, "Migration " + sp.getTitle(), workspace);
        }
    }

    private String getFolder(String buchname) {
        switch (buchname.trim()) {
        case "Benutzerhandbuch":
            return "handbuch";
        case "Prozessbeschreibung":
            return "prozesse";
        case "Tipps und Tricks":
            return "faq";
        case "Betriebshandbuch":
            return "betriebshandbuch";
        case "programmänderungen":
        case "Programmänderungen":
            return "releasenotes";
        default:
            String bn = buchname.toLowerCase().trim();
            if (bn.startsWith("programm") && bn.endsWith("nderungen")) {
                Logger.warn("Unbekannter Buchname: " + buchname + " -> folder: releaenotes   (spezial)");
                return "releasenotes";
            }
            String ret = "";
            for (int i = 0; i < bn.length(); i++) {
                char c = bn.charAt(0);
                if (c >= '0' && c <= '9' || c >= 'a' && c <= 'z') {
                    ret += c;
                }
            }
            Logger.warn("Unbekannter Buchname: " + buchname + " -> folder: " + ret);
            return ret;
        }
    }

    private void migratePage(ConfluencePage sp, SeiteSO tp, Map<String, String> files) {
        ConfluencePage en = findEnglishPage(sp);
        
        Seite seite = tp.getSeite();
        seite.setSorted(false);
        seite.getTitle().setString("de", sp.getTitle());
        seite.getTitle().setString("en", en == null ? "#en " + sp.getTitle() : en.getTitle());
        seite.getTags().addAll(sp.getLabels());

        String html = FileService.loadPlainTextFile(new File(htmlSourceFolder, sp.getId() + ".html"));
        html = migrateImages(tp, seite, html, files);
        tp.getContent().setString("de", processHTML(html));
        String html_en = "";
        if (en != null) {
            html_en = FileService.loadPlainTextFile(new File(htmlSourceFolder, en.getId() + ".html"));
            html_en = migrateImages(tp, seite, html_en, files);
            html_en = processHTML(html_en);
        }
        tp.getContent().setString("en", html_en);

        tp.saveMetaTo(files);
        tp.saveHtmlTo(files, langs);

        for (ConfluencePage sub : sp.getSubpages()) {
            SeiteSO subTp = tp.getSeiten().createSeite(tp, tp.getBook(), sub.getId());
            migratePage(sub, subTp, files);
        }
    }

    private ConfluencePage findEnglishPage(ConfluencePage de) {
        String enId = paerchen.get(de.getId());
        if (enId != null) {
            return findPage(root_en, enId);
        }
        return null;
    }

    private ConfluencePage findPage(ConfluencePage parent, String id) {
        if (parent.getId().equals(id)) {
            return parent;
        }
        for (ConfluencePage page : parent.getSubpages()) {
            ConfluencePage x = findPage(page, id);
            if (x != null) {
                return x;
            }
        }
        return null;
    }

    private String migrateImages(SeiteSO tp, Seite seite, String html, Map<String,String> files) {
        html = html.replace("img/", "img/" + seite.getId() + "/");
        Set<String> imgList = extract(html, "src=\"");
        for (String img : imgList) {
            int o = img.indexOf("/");
            if (o < 0) {
                throw new RuntimeException("'/' unexpected not found in image filename\n" + img);
            }
            int oo = img.indexOf("/", o + 1);
            if (oo < 0) {
                throw new RuntimeException("2nd '/' unexpected not found in image filename\n" + img);
            }
            String dnSource = img.substring(0, o) + img.substring(oo);
            String dnTarget = img;
            try {
                File srcImg = new File(htmlSourceFolder, dnSource);
                File targetImg = new File(tp.getBook().getFolder(), dnTarget);
                if (srcImg.isFile()) {
                    targetImg.getParentFile().mkdirs();
                    Files.copy(srcImg.toPath(), targetImg.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    files.put(targetImg.getAbsolutePath().replace("\\", "/"), DirAccess.IMAGE);
                } else {
                    Logger.error("src img ('" + img + "') not found: " + srcImg.toString());
                }
            } catch (IOException e) {
                Logger.error(e, "copy error for file: " + img);
                throw new RuntimeException("Error copying image file during migration.");
            }
        }
        return html;
    }

    private String processHTML(String html) {
        // Links
        Set<String> href = extract(html, "href=\"");
        for (String h : href) {
            if (h.startsWith("/html/") && h.endsWith(".html")) {
                String to = h.substring("/html/".length());
                to = to.substring(0, to.length() - ".html".length());
                html = html.replace(h, to);
            }
        }
        return html;
    }

    private Set<String> extract(String html, final String x1) {
        final String x2 = "\"";
        Set<String> set = new HashSet<>();
        int o = html.indexOf(x1);
        while (o >= 0) {
            o += x1.length();
            int oo = html.indexOf(x2, o);
            if (oo > o) {
                set.add(html.substring(o, oo));
            }
            o = html.indexOf(x1, oo + x2.length());
        }
        return set;
    }
}
