package minerva.migration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.ExclusionsSO;
import minerva.model.NotesSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.seite.Note;
import minerva.seite.Seite;
import minerva.user.UserAccess;

/**
 * This migration is based on the Confluence export for the online help (.html files).
 */
public class ConfluenceToMinervaMigrationService {
    private final File sourceFolder;
    private final File helpKeysFolder;
    private final WorkspaceSO workspace;
    private final List<String> langs;
    private ConfluencePage root_de;
    private ConfluencePage root_en;
    private File htmlSourceFolder;
    /** key: German page ID, value: English page ID */
    private Map<String, String> deEnMap;
    /** key: English page ID, value: German page ID */
    private Map<String, String> enDeMap;
    private List<EnglishSoloPage> englishSoloPages;
    private OldHelpKeysReader helpKeysCollection;
    private int imgErrors;
    
    public ConfluenceToMinervaMigrationService(File sourceFolder, File helpKeysFolder,
            WorkspaceSO workspace, List<String> langs) {
        this.sourceFolder = sourceFolder;
        this.helpKeysFolder = helpKeysFolder;
        this.workspace = workspace;
        this.langs = langs;
    }

    public void migrate() throws Exception {
        Logger.info("[Migration] Start of migration | source folder: " + sourceFolder.getAbsolutePath());
        imgErrors = 0;

        // Schritt 1: pullen, alle Dateien löschen und pushen
        workspace.pull();
        deleteWorkspace();
        workspace.pull(); // nochmal, um Objektstruktur neu aufzubauen
        Logger.info("Migration init phase completed");
        
        // Schritt 2: Hilfe-Keys laden
        helpKeysCollection = new OldHelpKeysReader();
        helpKeysCollection.readMappings(helpKeysFolder);

        // Schritt 3: Confluence Daten laden
        File csvFile = new File(sourceFolder, "html/mapping-tabelle-csv.csv");
        deEnMap = loadMappings(csvFile);
        enDeMap = new HashMap<>();
        for (Entry<String, String> e : deEnMap.entrySet()) {
            enDeMap.put(e.getValue(), e.getKey()); // create reversed map
        }
        englishSoloPages = loadMappings_soloEnId(csvFile);
        Logger.info("DE->EN mappings: " + deEnMap.size() + ", solo EN IDs: " + englishSoloPages.size());
        htmlSourceFolder = new File(sourceFolder, "html");
        loadHtmlFiles(htmlSourceFolder);

        // Schritt 4: Inhalte übertragen
        Logger.info("Migration main part starts");
        int position = 1;
        for (ConfluencePage sp : root_de.getSubpages()) {
            migrateBook(sp, position++);
        }
        Logger.info("image migration errors: " + imgErrors + " (see log)");
        Logger.info("---- Migration finished ----");
    }

    private void deleteWorkspace() {
        File[] files = new File(workspace.getFolder()).listFiles();
        if (files == null) {
            throw new RuntimeException("files is null");
        }
        Set<String> filenames = new HashSet<>();
        String x = workspace.getFolder() + "/";
        for (File file : files) {
            if (file.getName().startsWith(".") || file.getName().equals(ExclusionsSO.DN)) {
            } else if (file.isDirectory()) {
                FileService.deleteFolder(file);
                filenames.add(file.getAbsolutePath().replace("\\", "/").replace(x, ""));
            } else {
                file.delete();
                filenames.add(file.getAbsolutePath().replace("\\", "/").replace(x, ""));
            }
        }
        if (filenames.isEmpty()) {
            Logger.info("[Migration] deleteWorkspace() has nothing to do");
        } else {
            MinervaWebapp.factory().getBackendService().saveFiles(
                    new MigrationCommitMessage("empty branch " + workspace.getBranch()),
                    workspace, new HashSet<>(), filenames);
            Logger.info("[Migration] deleteWorkspace() ok");
        }
    }

    private void loadHtmlFiles(File confluenceHtmlDir) {
        Logger.info("readHtmlFiles: " + confluenceHtmlDir.getAbsolutePath());
        ConfluencePage root = FileService.loadJsonFile(new File(confluenceHtmlDir, "data.json"), ConfluencePage.class);
        root_de = root.getSubpages().get(0);
        root_en = root.getSubpages().get(1);
    }

    private String loadHtmlFile(String id) {
		return FileService.loadPlainTextFile(new File(htmlSourceFolder, id + ".html"));
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

        // migrate pages ----
        for (ConfluencePage sub : sp.getSubpages()) {
            SeiteSO subTp = book.getSeiten().createSeite(book.getISeite(), book, sub.getId());
            migratePage(sub, subTp, files);
        }
        
        // migrate solo English pages ----
        migrateEnglishSoloPages(sp, files, book);

        // commit and push everything ----
        Logger.info("saving " + files.size() + " files for book \"" + sp.getTitle() + "\"...");
        if (!files.isEmpty()) {
            MigrationCommitMessage cm = new MigrationCommitMessage(sp.getTitle());
            cm.bigCommit();
            workspace.dao().saveFiles(files, cm, workspace);
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
                Logger.warn("Unknown book name: " + buchname + " -> folder: releaenotes   (special)");
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
        seite.getTitle().setString("de", removeUnderscores(sp.getTitle()));
        seite.getTitle().setString("en", en == null ?
                "//en " + removeUnderscores(sp.getTitle()) : removeUnderscores(en.getTitle()));
        seite.getTags().addAll(sp.getLabels());

        String html = loadHtmlFile(sp.getId());
        html = migrateImages(tp, seite, html, files);
        tp.getContent().setString("de", processHTML(html));
        String html_en = "";
        if (en != null) {
            html_en = loadHtmlFile(en.getId());
            html_en = migrateImages(tp, seite, html_en, files);
            html_en = processHTML(html_en);
        }
        tp.getContent().setString("en", html_en);
        migrateHelpKeys(sp, en, tp);
        MinervaWebapp.factory().getPageChangeStrategy().set("Migration", tp);
        
        migrateNotes(sp.getId(), en == null ? null : en.getId(), tp, files);

        tp.saveMetaTo(files);
        tp.saveHtmlTo(files, langs);

        for (ConfluencePage sub : sp.getSubpages()) {
            SeiteSO subTp = tp.getSeiten().createSeite(tp, tp.getBook(), sub.getId());
            migratePage(sub, subTp, files);
        }
    }

	private String removeUnderscores(String title) {
        if (title == null) {
            return "//null";
        }
        title = title.replace("_", " ");
        while (title.contains("  ")) {
            title = title.replace("  ", " ");
        }
        return title.isBlank() ? "//empty" : title.trim();
    }

    private void migrateEnglishPage(ConfluencePage sp, SeiteSO tp, Map<String, String> files) {
        Seite seite = tp.getSeite();
        seite.setSorted(false);
        seite.getTitle().setString("de", "//" + removeUnderscores(sp.getTitle()));
        seite.getTitle().setString("en", removeUnderscores(sp.getTitle()));
        seite.getTags().addAll(sp.getLabels());

        String html = loadHtmlFile(sp.getId());
        html = migrateImages(tp, seite, html, files);
        tp.getContent().setString("de", "");
        tp.getContent().setString("en", processHTML(html));

        tp.saveMetaTo(files);
        tp.saveHtmlTo(files, langs);

        for (ConfluencePage sub : sp.getSubpages()) {
            SeiteSO subTp = tp.getSeiten().createSeite(tp, tp.getBook(), sub.getId());
            migrateEnglishPage(sub, subTp, files);
        }
    }

    private ConfluencePage findEnglishPage(ConfluencePage de) {
        String enId = deEnMap.get(de.getId());
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
            	Logger.error("'/' unexpected not found in image filename: " + img + " | skip | " + tp.getTitle());
            	imgErrors++;
            	continue;
            }
            int oo = img.indexOf("/", o + 1);
            if (oo < 0) {
                Logger.error("2nd '/' unexpected not found in image filename: " + img + " | skip | " + tp.getTitle());
            	imgErrors++;
                continue;
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
                	imgErrors++;
                }
            } catch (IOException e) {
                Logger.error(e, "copy error for file: " + img);
            	imgErrors++;
            }
        }
        return html;
    }

    private String processHTML(String html) {
        // remove toc
        // remove <style>
        if (html.contains("toc-macro") || html.contains("<style")) {
            Document doc = Jsoup.parse(html);
            doc.selectXpath("//div[contains(@class,'toc-macro')]").remove();
            doc.selectXpath("//style").remove();
            html = doc.toString();
        }
        // <h1> runterstufen usw.
        for (int h = 6; h >= 1; h--) {
            int n = h + 1;
            html = html.replace("<h" + h, "<h" + n).replace("</h" + h + ">", "</h" + n + ">");
        }
        // Links
        Set<String> href = extract(html, "href=\"");
        for (String h : href) {
            if (h.contains("/pages/createpage.action")) {
                html = html.replace(h, "#"); // kill it
            } else if (h.startsWith("/html/") && h.endsWith(".html")) {
                String to = h.substring("/html/".length());
                to = to.substring(0, to.length() - ".html".length());
                to = englishToGermanLink(to);
                html = html.replace(h, to);
            }
        }
        return html;
    }

    private String englishToGermanLink(String href) {
        String ret = enDeMap.get(href);
        return ret == null ? href : ret;
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
    
    private Map<String, String> loadMappings(File csvFile) {
        Map<String, String> map = new HashMap<>(); // key: de ID, value: en ID
        String content = FileService.loadPlainTextFile(csvFile);
        if (content == null) {
            throw new RuntimeException("File not found: " + csvFile.getAbsolutePath());
        }
        String[] lines = content.split("\r\n");
        for (int i = 1 /* omit 1st line */; i < lines.length; i++) {
            String line = lines[i];
            String[] col = line.split(";");
            String deId = col.length > 2 ? col[2] : "";
            String enId = col.length > 6 ? col[6] : "";
            if (!deId.isEmpty() && !enId.isEmpty()) {
                map.put(deId, enId);
            }
        }
        return map;
    }

    private List<EnglishSoloPage> loadMappings_soloEnId(File csvFile) {
        List<EnglishSoloPage> ret = new ArrayList<>();
        String[] lines = FileService.loadPlainTextFile(csvFile).split("\r\n");
        for (int i = 1 /* omit 1st line */; i < lines.length; i++) {
            String line = lines[i];
            String[] col = line.split(";");
            String deId = col.length > 2 ? col[2].trim() : "";
            String enId = col.length > 6 ? col[6].trim() : "";
            if (deId.isEmpty() && !enId.isEmpty()) {
                String parentId = col.length > 8 ? col[8].trim() : "";
                if (parentId.startsWith("parent=")) {
                    parentId = parentId.substring("parent=".length()).trim();
                } else {
                    parentId = "";
                }
                ret.add(new EnglishSoloPage(enId, parentId));
            }
        }
        return ret;
    }

    private void migrateEnglishSoloPages(ConfluencePage sp, Map<String, String> files, BookSO book) {
        if ("Benutzerhandbuch".equals(sp.getTitle())) {
            Logger.info("Migrating " + englishSoloPages.size() + " solo English pages...");
            final SeiteSO parent = book.getSeiten().createSeite(book.getISeite(), book, IdGenerator.createId6());
            parent.getSeite().getTitle().setString("de", "//English solo pages");
            parent.getSeite().getTitle().setString("en", "English solo pages");
            parent.getSeite().getTags().add("nicht_oh");
            parent.getSeite().getTags().add("english-solo-pages");
            parent.saveMetaTo(files);
            for (EnglishSoloPage englishSoloPage : englishSoloPages) {
                ConfluencePage page = findPage(root_en, englishSoloPage.getId());
                if (page == null) {
                    Logger.error("English solo page not found: " + englishSoloPage.getId());
                } else {
                    SeiteSO theParent = parent;
                    if (!englishSoloPage.getParentId().isEmpty()) {
                        final SeiteSO parent2 = book._seiteById(englishSoloPage.getParentId());
                        if (parent2 != null) {
                            theParent = parent2;
                        } else {
                            Logger.error("Parent page " + englishSoloPage.getParentId()
                                    + " not found for English solo page " + englishSoloPage.getId());
                        }
                    }
                    SeiteSO subTp = theParent.getSeiten().createSeite(theParent, theParent.getBook(), englishSoloPage.getId());
                    migrateEnglishPage(page, subTp, files);
                }
            }
        }
    }

    private void migrateHelpKeys(ConfluencePage de, ConfluencePage en, SeiteSO tp) {
        List<String> helpKeysTarget = tp.getSeite().getHelpKeys();
        helpKeysTarget.clear();
        if (de != null) {
            addHelpKeys(helpKeysCollection.getHelpKeys(de.getId()), helpKeysTarget);
        }
        if (en != null) {
            addHelpKeys(helpKeysCollection.getHelpKeys(en.getId()), helpKeysTarget);
        }
        Collections.sort(helpKeysTarget);
    }

    private void addHelpKeys(List<String> source, List<String> target) {
        if (source == null) {
            return;
        }
        for (String helpKey : source) {
            if (!target.contains(helpKey)) {
                target.add(helpKey);
            }
        }
    }

    private void migrateNotes(String deId, String enId, SeiteSO seite, Map<String, String> files) {
    	ConfluenceComments deNotes = loadNotes(deId);
    	ConfluenceComments enNotes = loadNotes(enId);
    	if (deId.equals(enId)) {
    		throw new RuntimeException("deId is = enId! " + deId);
    	}
    	boolean de_en_Hinweis = !deNotes.getComments().isEmpty() && !enNotes.getComments().isEmpty();
    	migrateNotes2("deutsche",  deNotes, de_en_Hinweis, seite, files);
    	migrateNotes2("englische", enNotes, de_en_Hinweis, seite, files);
    }
    
	private ConfluenceComments loadNotes(String id) {
		ConfluenceComments ret = null;
		if (!StringService.isNullOrEmpty(id)) {
			File file = new File(sourceFolder, "html/notes/" + id + ".json");
			if (file.isFile()) {
				ret = FileService.loadJsonFile(file, ConfluenceComments.class);
			}
		}
		if (ret == null) {
			ret = new ConfluenceComments();
		}
		if (ret.getComments() == null) {
			ret.setComments(new ArrayList<>());
		}
		return ret;
	}
	
	private void migrateNotes2(String lang, ConfluenceComments cnotes, boolean de_en_Hinweis, SeiteSO seite, Map<String, String> files) {
		int n = cnotes.getComments().size();
		if (n > 0) {
			NotesSO notesSO = seite.notes();
			migrateNotes3(lang, cnotes.getComments(), null, de_en_Hinweis, notesSO, files);
			/* Zwischennotiz
			Note mnote = notesSO.createNote(null,
					"// Die Kommentare oberhalb stammen aus dem Altsystem und beziehen sich auf die " + lang + " Seite.",
					"Minerva", notesSO.now(), new ArrayList<>());
    		notesSO.saveTo(mnote, files);*/
		}
	}
	
	private void migrateNotes3(String lang, List<ConfluenceComment> cnotes, Note mnote_parent, boolean de_en_Hinweis,
			NotesSO notesSO, Map<String, String> files) {
    	for (ConfluenceComment cnote : cnotes) {
    		String text = cnote.getPlainText(); // must be called before getPersons()
			Note mnote = notesSO.createNote(mnote_parent,
					(de_en_Hinweis ? "(Diese Notiz bezieht sich auf die " + lang + " Seite.)\n" : "") +
					text,
					realName2Login(cnote.getAuthor()),
					transformDate(cnote.getCreated()),
					cnote.getPersons().stream().map(p -> realName2Login(p)).collect(Collectors.toList()));
    		notesSO.saveTo(mnote, files);
    		migrateNotes3(lang, cnote.getComments(), mnote, de_en_Hinweis, notesSO, files); // recursive
		}
	}
	
	private String realName2Login(String realname) {
		String login = UserAccess.realName2Login(realname);
		return login == null ? realname : login;
	}

	private String transformDate(String date) { // 2022-10-05T14:10:32 -> 2022-10-05 14:10
		return date.substring(0, 10) + " " + date.substring(11, 16);
	}
}
