package minerva.model;

import static minerva.base.FileService.moveFiles;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.FileService;
import minerva.base.StringService;
import minerva.comment.SeiteCommentService;
import minerva.config.MinervaFactory;
import minerva.papierkorb.WSeite;
import minerva.papierkorb.WeggeworfeneSeite;
import minerva.seite.Seite;

public class PapierkorbSO {
    private final WorkspaceSO workspace;
    private final String folder;
    private final List<String> langs;
    
    public PapierkorbSO(WorkspaceSO workspace) {
        this.workspace = workspace;
        MinervaFactory fac = MinervaWebapp.factory();
        folder = fac.getConfig().getWorkspacesFolder() + "/papierkorb/" + workspace.getBranch();
        langs = fac.getLanguages();
    }
    
    public List<WeggeworfeneSeite> list() {
        return rawlist()
                .filter(s -> s != null)
                .sorted((a, b) -> b.getDeleteDate().compareTo(a.getDeleteDate()))
                .collect(Collectors.toList());
    }
    
    public int countSubpages(WSeite ws) {
        int ret = ws.getUnterseiten().size();
        for (WSeite sub : ws.getUnterseiten()) {
            ret += countSubpages(sub); // recursive
        }
        return ret;
    }
    
    public void push(SeiteSO seite) {
        if (MinervaWebapp.factory().isGitlab()) {
            return;
        }
        String id = seite.getId();
        pushSeite(seite, folder + "/" + id + "/seite/");
        
        // papierkorb.json
        WeggeworfeneSeite ws = new WeggeworfeneSeite();
        fillWSeite(seite, ws);
        ws.setBookFolder(seite.getBook().getBook().getFolder());
        if (seite.hasParent()) {
            ws.setParentId(seite.getSeite().getParentId());
            ws.getParentTitle().from(seite.getParent().getSeite().getTitle());
        }
        ws.setDeleteDate(StringService.now());
        ws.setDeletedBy(workspace.getUser().getLogin());
        FileService.saveJsonFile(new File(dnPapierkorbJson(id)), ws);
    }
    
    private void pushSeite(SeiteSO seite, String target) {
        String id = seite.getId();
        Logger.info(workspace.getUser().getLogin() + " | Papierkorb push " + id + " \"" + seite.getTitle() + "\"");
        String source = seite.getBook().getFolder() + "/";

        // <id>.meta
        copy(source + id + ".meta", target);

        // <lang>/<id>.html
        langs.forEach(lang -> copy(source + lang + "/" + id + ".html", target + lang));

        // img/<id>/*
        copyAll(source + "img/" + id, target + "img/" + id);

        // comments/<id>/*.json
        copyAll(source + SeiteCommentService.FOLDER + "/" + id, target + SeiteCommentService.FOLDER + "/" + id);

        // recursive
        seite.getSeiten().forEach(sub -> pushSeite(sub, target));
    }

    // Für die Dateianlage soll nicht DirAccess benutzt werden, da der Papierkorb nicht ins Git soll.
    private void copy(String fromFile, String toFile) {
        File f = new File(fromFile);
        if (f.isFile()) {
            FileService.copyFile(f, new File(toFile));
        }
    }

    private void copyAll(String fromDir, String toDir) {
        File f = new File(fromDir);
        if (f.isDirectory()) {
            FileService.copyFiles(f, new File(toDir));
        }
    }

    private void fillWSeite(SeiteSO seite, WSeite ws) {
        ws.setId(seite.getId());
        ws.getTitle().from(seite.getSeite().getTitle());
        addWSeiten(seite, ws); // recursive
    }
    
    private void addWSeiten(SeiteSO seite, WSeite parent) {
        for (SeiteSO sub : seite.getSeiten()) {
            WSeite ws = new WSeite();
            fillWSeite(sub, ws);
            parent.getUnterseiten().add(ws);
        }
    }
    
    public String pop(String id) {
        WeggeworfeneSeite ws = byId(id);
        BookSO book = getBook(ws);
        String parentId = getParentId(book, ws);
        pop(ws, parentId, book);
        
        String pl = workspace.getUser().getPageLanguage();
        String title = ws.getTitle().getString(pl);
        Logger.info(workspace.getUser().getLogin() + " | " + workspace.getBranch() + " | Restored page " + ws.getId() + " \"" + title + "\"");
        return "/s/" + workspace.getBranch() + "/" + book.getBook().getFolder() + "/" + ws.getId();
    }
    
    private BookSO getBook(WeggeworfeneSeite ws) {
        try {
            return workspace.getBooks().byFolder(ws.getBookFolder());
        } catch (Exception e) {
            BookSO book = workspace.getBooks().get(0);
            Logger.warn("RecycleAction (" + ws.getId() + "): Book " + ws.getBookFolder()
                    + " does not exist. Take other book: " + book.getBook().getFolder());
            return book;
        }
    }

    private String getParentId(BookSO book, WeggeworfeneSeite ws) {
        if (ws.getParentId() != null) {
            SeiteSO seite = book._seiteById(ws.getParentId());
            if (seite != null) {
                return seite.getId();
            } else {
                String pl = workspace.getUser().getPageLanguage();
                Logger.warn("pop(" + ws.getId() + ").getParent: Parent page \"" + ws.getParentTitle().getString(pl)
                        + "\" does not exist anymore. The restored page is inserted at the top book level.");
            }
        }
        return SeiteSO.ROOT_ID; // take book
    }

    public void pop(WeggeworfeneSeite ws, String parentId, BookSO bookSO) {
        String id = ws.getId();
        Logger.info(workspace.getUser().getLogin() + " | Papierkorb pop " + id + " \"" + ws.getTitle().getString(langs.get(0)) + "\"");
        moveFiles(new File(folder + "/" + id + "/seite"), new File(bookSO.getFolder()));
        File f = new File(dnPapierkorbJson(id));
        f.delete();
        FileService.deleteFolder(f.getParentFile());
        // Position müsste eigentlich ggf. korrigiert werden. Ich lass das erstmal.
        
        File meta = new File(bookSO.getFolder() + "/" + id + ".meta");
        Seite seite = FileService.loadJsonFile(meta, Seite.class);
        if (!parentId.equals(seite.getParentId())) {
            Logger.info("pop(): change parent ID from " + seite.getParentId() + " to " + parentId);
            seite.setParentId(parentId);
            FileService.saveJsonFile(meta, seite);
        }
        
        workspace.pull();
    }
    
    public void delete(String id) {
        Logger.info(workspace.getUser().getLogin() + " | Papierkorb delete " + id);
        File dir = new File(dnPapierkorbJson(id)).getParentFile();
        if ("_all".equals(id)) { // Papierkorb leeren
            dir = dir.getParentFile();
        }
        if (dir.isDirectory()) {
            FileService.deleteFolder(dir);
        } else {
            Logger.error("Recycle bin entry #" + id + " does not exist! dir: " + dir.getAbsolutePath());
        }
    }
    
    private String dnPapierkorbJson(String dir) {
        return folder + "/" + dir + "/papierkorb.json";
    }

    public WeggeworfeneSeite byId(String id) {
        return rawlist().filter(i -> i != null && i.getId().equals(id)).findFirst().orElseThrow(() -> new RuntimeException("Recycle bin entry does not exist!"));
    }

    private Stream<WeggeworfeneSeite> rawlist() {
        MultiPurposeDirAccess dao = new MultiPurposeDirAccess(workspace.dao());
        return workspace.dao().getAllFolders(folder).stream()
                .map(dir -> dao.load(dnPapierkorbJson(dir), WeggeworfeneSeite.class));
    }
}
