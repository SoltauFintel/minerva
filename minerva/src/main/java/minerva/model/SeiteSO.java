package minerva.model;

import static minerva.access.DirAccess.IMAGE;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.seite.ChangeFile;
import minerva.seite.IMoveFile;
import minerva.seite.MoveFile;
import minerva.seite.Seite;

public class SeiteSO implements ISeite {
    public static final String META_SUFFIX = ".meta";
    public static final String ROOT_ID = "root";
    private final BookSO book;
    private Seite seite;
    /** Unterseiten */
    private SeitenSO seiten;
    /** images for upload by DirAccess */
    private final List<String> images = new ArrayList<>();

    // TODO wenn ich in Buch 1 bin, dann sollte ich nicht die contents für die anderen Bücher im Speicher haben. Ich kann die ja immer schnell von der Platte laden.
    /** null: nicht geladen */
    private NlsString content = null;
    private final NotesSO notes = new NotesSO(this);
    private final TagsSO tags = new TagsSO(this);
    
    /**
     * Loaded Seite constructor
     * @param book -
     * @param seite -
     * @param alleSeiten -
     */
    public SeiteSO(BookSO book, Seite seite, List<Seite> alleSeiten) {
        // unterseiten zum Schluss setzen!
        this.book = book;
        this.seite = seite;
        seiten = SeitenSO.findeUnterseiten(this, alleSeiten, book);
    }
    
    /**
     * copy constructor: only seite is a copy
     * @param c -
     */
    public SeiteSO(SeiteSO c) {
        this.book = c.book;
        this.seite = new Seite(c.seite); // copy
        this.seiten = c.seiten;
        this.images.addAll(c.images);
        this.content = c.content;
    }

    /**
     * New Seite constructor
     * @param book -
     * @param seite -
     * @param parent -
     */
    public SeiteSO(BookSO book, Seite seite, ISeite parent) {
        this.book = book;
        this.seite = seite;
        seiten = new SeitenSO(parent); // leer
    }
    
    public Seite getSeite() {
        return seite;
    }

    @Override
    public SeitenSO getSeiten() {
        return seiten;
    }

    public SeitenSO getSeiten(String lang) {
        seiten.sort(lang);
        return seiten;
    }

    @Override
    public String getId() {
        return seite.getId();
    }

    @Override
    public boolean isSorted() {
        return seite.isSorted();
    }

    @Override
    public String getTitle() {
        return seite.getTitle().getString(book.getUser().getPageLanguage());
    }
    
    public String getSortTitle() {
        return getTitle().toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
    }

    public String getSortTitle(String lang) {
        return getSeite().getTitle().getString(lang).toLowerCase()
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
    }

    public BookSO getBook() {
        return book;
    }
    
    public String getSort() {
        return new DecimalFormat("000").format(book.getBook().getPosition()) + getTitle();
    }
    
    public boolean hasNoParent() {
        return ROOT_ID.equals(seite.getParentId());
    }

    public String getLogin() {
        return book.getUser().getUser().getLogin();
    }

    public NlsString getContent() {
        if (content == null) {
            content = new NlsString();
            List<String> langs = MinervaWebapp.factory().getLanguages();
            Map<String, String> files = loadFiles(false, langs);
            setContent(files, langs);
        }
        return content;
    }

    public void forceReloadIfCheap() {
        if (!MinervaWebapp.factory().isGitlab()) {
            seite = new MultiPurposeDirAccess(dao()).load(filenameMeta(), Seite.class);
            content = null;
        }
    }

    /**
     * Prüfen, ob Content in Dateien neuer als Content im Hauptspeicher ist.
     */
    public void freshcheck(List<String> langs) {
        // Diese Methode nicht in ViewSeitePage.execute() aufrufen, da das sonst ein doofes
        // Verhalten beim Speichern gibt.
        if (content == null) {
            content = new NlsString();
            // Jetzt muss ich auf jeden Fall laden!
        }
        // naive Implementierung: ich lade kompletten Content einfach neu
        Map<String, String> files = loadFiles(true, langs);
        seite = new Gson().fromJson(files.get(filenameMeta()), Seite.class);
        setContent(files, langs);
    }
    
    private Map<String, String> loadFiles(boolean withSeite, List<String> langs) {
        Set<String> filenames = new HashSet<>();
        if (withSeite) {
            filenames.add(filenameMeta());
        }
        for (String lang : langs) {
            filenames.add(filenameHtml(lang));
        }
        return dao().loadFiles(filenames);
    }

    private void setContent(Map<String, String> files, List<String> langs) {
        for (String lang : langs) {
            content.setString(lang, files.get(filenameHtml(lang)));
        }
    }

    public List<String> getImages() {
        return images;
    }

    public void activateSorted() {
        seite.setSorted(true);
        saveMeta("sort subpages alphabetically: $t");
        book.getWorkspace().pull(); // ja, ist etwas brutal...
    }

    public TagsSO tags() {
        return tags;
    }
    
    public NotesSO notes() {
        return notes;
    }
    
    public void remove() {
        Set<String> filenamesToDelete = new HashSet<>();
        List<String> langs = MinervaWebapp.factory().getLanguages();
        remove(filenamesToDelete, langs);

        List<String> cantBeDeleted = new ArrayList<>();
        dao().deleteFiles(filenamesToDelete, "delete page " + getId(), book.getWorkspace(), cantBeDeleted);
        boolean success = cantBeDeleted.isEmpty();

        if (success) {
            new Thread(() -> {  // Access index in background so user does not have to wait for completion.
                book.getWorkspace().getSearch().unindex(SeiteSO.this);
                Logger.debug("Deleted page " + SeiteSO.this.getId() + " has been removed from search index.");
            }).start();
        }
        
        book.getWorkspace().pull(); // Datenstruktur neu aufbauen (auch wenn cantBeDeleted nicht leer ist)
        
        if (success) {
            Logger.info("Delete page " + getId() + " complete.");
        } else {
            Logger.error("Tried to delete page " + getId() + ": These files could not be deleted:"
                    + cantBeDeleted.stream().map(i -> "\n  - " + i).collect(Collectors.joining())
                    + "\n  Please delete these files manually.");
            throw new RuntimeException("Es konnten nicht alle Dateien gelöscht werden!" // TODO UserMessage
                    + "\nBitte an Administrator wenden. ID " + getId());
        }
    }
    
    private void remove(Set<String> filenamesToDelete, List<String> langs) {
        // Untergeordnete Seiten
        for (SeiteSO unterseite : seiten) {
            unterseite.remove(filenamesToDelete, langs); // rekursiv
        }
        
        // Images für diese Seite
        filenamesToDelete.add(book.getFolder() + "/img/" + getId() + "/*");
        
        // Seite selbst (.meta, .html)
        filenamesToDelete.add(filenameMeta());
        langs.forEach(lang -> filenamesToDelete.add(filenameHtml(lang)));
    }

    public void move(String parentId) {
        // Seite bei neuer Parent-Seite hinzfuügen
        if (SeiteSO.ROOT_ID.equals(parentId)) {
            seite.setPosition(book.getSeiten().calculateNextPosition());
            book.getSeiten().add(this);
        } else {
            SeiteSO parentSeite = book.getSeiten().byId(parentId); // soll auch prüfen, ob parentId gültig ist
            seite.setPosition(parentSeite.getSeiten().calculateNextPosition());
            parentSeite.getSeiten().add(this);
        }
        // Seite bei alter Parent-Seite entfernen
        if (SeiteSO.ROOT_ID.equals(seite.getParentId()) ){
            book.getSeiten().onlyRemove(this);
        } else {
            book.getSeiten().byId(seite.getParentId()).getSeiten().onlyRemove(this);
        }
        seite.setParentId(parentId);
        saveMeta("moved page " + getTitle());
        book.getWorkspace().pull();
    }

    public void moveToBook(String targetBookFolder, List<String> langs) {
        if (book.getBook().getFolder().equals(targetBookFolder)) {
            throw new RuntimeException("Target book can't be current book! Moving page to other book failed.");
        }
        WorkspaceSO workspace = book.getWorkspace();
        BookSO targetBook = workspace.getBooks().byFolder(targetBookFolder);
        int newPosition = targetBook.getSeiten().calculateNextPosition();
        
        List<IMoveFile> files = new ArrayList<>();
        changePageTo(newPosition, files);
        movePageToBookTo(targetBook, langs, files);
        String commitMessage = "moved page \"" + getTitle() + "\" (#" + getId() + ") to book " + targetBookFolder;
        dao().moveFiles(files, commitMessage, workspace);
        
        workspace.pull();
    }
    
    private void changePageTo(int newPosition, List<IMoveFile> files) {
        seite.setParentId(ROOT_ID);
        seite.setPosition(newPosition);
        files.add(new ChangeFile(filenameMeta(), StringService.prettyJSON(seite)));
    }
    
    private void movePageToBookTo(BookSO targetBook, List<String> langs, List<IMoveFile> files) {
        files.add(new MoveFile(filenameMeta(), targetBook.getFolder() + "/" + getId() + META_SUFFIX));
        
        // html files for all languages
        for (String lang : langs) {
            files.add(new MoveFile(filenameHtml(lang), targetBook.getFolder() + "/" + lang + "/" + getId() + ".html"));
        }
        
        // img folder
        files.add(new MoveFile(book.getFolder() + "/img/" + getId(), targetBook.getFolder() + "/img/" + getId()));
        
        // subpages
        for (SeiteSO seite : seiten) {
            seite.movePageToBookTo(targetBook, langs, files); // recursive
        }
    }

    public void saveAll(NlsString newTitle, NlsString newContent, int version, List<String> langs) {
        if (getSeite().getVersion() != version) {
            throw new UserMessage("error.simultaneousEditing", book.getWorkspace());
        }
        for (String lang : langs) {
            if (StringService.isNullOrEmpty(newTitle.getString(lang))) {
                throw new UserMessage("error.enterTitle", book.getWorkspace());
            }
        }
        
        if (content == null) {
            content = new NlsString();
        }
        for (String lang : langs) {
            seite.getTitle().setString(lang, newTitle.getString(lang));
            content.setString(lang, newContent.getString(lang));
        }
        
        Map<String, String> files = new HashMap<>();
        saveMetaTo(files);
        saveHtmlTo(files, langs);
        images.forEach(filename -> files.put(filenameImage(filename), IMAGE));
        
        dao().saveFiles(files, getTitle(), book.getWorkspace());
        
        images.clear();

        if (hasNoParent()) {
            // Wenn book.sorted=true ist und ein Seitentitel geändert worden ist, muss neu sortiert werden.
            book.getSeiten().sort();
        }
        
        new Thread(() -> {  // Access index in background so user does not have to wait for completion.
            book.getWorkspace().getSearch().index(SeiteSO.this);
            Logger.debug("Page " + SeiteSO.this.getId() + " has been reindexed.");
        }).run();
    }

    public void saveMeta(String commitMessage) {
        Map<String, String> files = new HashMap<>();
        saveMetaTo(files);
        saveFiles(files, commitMessage);
    }

    public void saveHtml(String commitMessage, List<String> langs) {
        Map<String, String> files = new HashMap<>();
        saveHtmlTo(files, langs);
        saveFiles(files, commitMessage);
    }

    public void saveMetaTo(Map<String,String> files) {
        seite.setVersion(seite.getVersion() + 1);
        files.put(filenameMeta(), StringService.prettyJSON(seite));
    }

    public void saveHtmlTo(Map<String,String> files, List<String> langs) {
        langs.forEach(lang -> files.put(filenameHtml(lang), StringService.prettyHTML(content.getString(lang))));
    }

    // similar method in BookSO
    public void saveSubpagesAfterReordering(SeitenSO reorderdSeiten) {
        this.seiten = reorderdSeiten;
        Map<String, String> files = new HashMap<>();
        if (seite.isSorted()) {
            seite.setSorted(false);
            saveMetaTo(files);
        }
        reorderdSeiten.setPositionsAndSaveTo(files);
        saveFiles(files, "reordering subpages of: $t");
    }

    private void saveFiles(Map<String, String> files, String commitMessage) {
        dao().saveFiles(files, commitMessage.replace("$t", getTitle()), book.getWorkspace());
    }

    public String filenameMeta() {
        return book.getFolder() + "/" + getId() + META_SUFFIX;
    }
    
    public String gitFilenameMeta() {
        return book.getBook().getFolder() + "/" + getId() + META_SUFFIX;
    }
    
    private String filenameHtml(String lang) {
        return book.getFolder() + "/" + lang + "/" + getId() + ".html";
    }

    private String filenameImage(String filename) {
        return book.getFolder() + "/" + filename;
    }

    private DirAccess dao() {
        return book.dao();
    }

    /**
     * @return 1: page is not empty, 2: page is empty, but at least one subpage is not empty,
     * 0: page and subpages are empty, 3: error (which should be interpreted as "page is not empty"
     * to be on the safe side)
     */
    public int hasContent(String lang) {
        // In theory, this approach is a bit expensive since all content must be loaded and must be parsed.
        // However in practice it takes less than 0.4 seconds on the first call.
        try {
            String html = getContent().getString(lang);
            Document doc = Jsoup.parse(html);
            Elements body = doc.select("body");
            if (body != null && !body.isEmpty() && body.get(0).childrenSize() > 0) {
                return 1;
            }
            for (SeiteSO sub : seiten) {
                if (sub.hasContent(lang) > 0) {
                    return 2;
                }
            }
            return 0;
        } catch (Exception e) {
            Logger.error(e);
            return 3;
        }
    }
}
