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
import minerva.access.CommitMessage;
import minerva.access.DirAccess;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.exclusions.Exclusions;
import minerva.exclusions.ExclusionsService;
import minerva.seite.IPageChangeStrategy;
import minerva.seite.NotifyWatchers;
import minerva.seite.PageChange;
import minerva.seite.Seite;
import minerva.seite.TocMacroPage;
import minerva.seite.move.ChangeFile;
import minerva.seite.move.IMoveFile;
import minerva.seite.move.MoveFile;
import minerva.subscription.SubscriptionService;
import minerva.subscription.TPage;

public class SeiteSO implements ISeite {
    public static final String META_SUFFIX = ".meta";
    public static final String ROOT_ID = "root";
    private final BookSO book;
    private Seite seite;
    /** Unterseiten */
    private SeitenSO seiten;
    /** images for upload by DirAccess */
    private final List<String> images = new ArrayList<>();

    /** null: nicht geladen */
    private NlsString content = null;
    private final NotesSO notes = new NotesSO(this);
    private final TagsSO tags = new TagsSO(this);
    /** true: it's a new page that has never been saved, false: already existing page */
    private boolean neu = false;
    
    /**
     * Loaded Seite constructor
     * @param book -
     * @param seite -
     * @param alleSeiten -
     */
    public SeiteSO(BookSO book, Seite seite, List<Seite> alleSeiten) {
        if (seite == null) {
            throw new IllegalArgumentException("seite is null");
        }
        // unterseiten zum Schluss setzen!
        this.book = book;
        this.seite = seite;
        seiten = SeitenSO.findeUnterseiten(this, alleSeiten, book);
        notes.load(); // TODO Es gibt Anwendungsfälle bei denen die Notizen gar nicht benötigt werden.
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
        if (seite == null) {
            throw new IllegalArgumentException("seite is null");
        }
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
    public boolean isReversedOrder() {
        return seite.getTags().contains("reversed-order");
    }

    @Override
    public String getTitle() {
        return seite.getTitle().getString(book.getUser().getPageLanguage());
    }
    
    public String getSortTitle() {
        return StringService.umlaute(getTitle());
    }

    public String getSortTitle(String lang) {
        return StringService.umlaute(getSeite().getTitle().getString(lang));
    }

    public BookSO getBook() {
        return book;
    }
    
    public String getSort() {
        return new DecimalFormat("000").format(book.getBook().getPosition()) + getTitle();
    }

    public boolean isNeu() {
        return neu;
    }

    public void setNeu(boolean neu) {
        this.neu = neu;
    }

    public boolean hasParent() {
        return !ROOT_ID.equals(seite.getParentId());
    }
    
    /**
     * Use hasParent() before!
     * @return parent page
     */
    public SeiteSO getParent() {
        return book.seiteById(seite.getParentId());
    }

    public String getLogin() {
        return book.getUser().getLogin();
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
        Seite s = MinervaWebapp.factory().getBackendService().forceReloadIfCheap(filenameMeta());
        if (s == null) { // any problem, then just cancel
            return;
        }
        seite = s;
        content = null; // force reload
        notes.load();
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
        String dn = filenameMeta();
        String json = files.get(dn);
        if (json == null) {
            throw new RuntimeException("There is no content for " + dn + ". Error in freshcheck.");
        }
        seite = new Gson().fromJson(json, Seite.class);
        if (seite == null) {
            throw new RuntimeException("seite is null after loading .meta file. Error in freshcheck. dn: " + dn);
        }
        setContent(files, langs);
        notes.load();
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
        saveMeta(new CommitMessage(this, "subpages sorted alphabetically"));
        book.getWorkspace().pull(); // ja, ist etwas brutal...
        new SubscriptionService().pagesChanged();
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
        dao().deleteFiles(filenamesToDelete,
                new CommitMessage(this, "page deleted"), book.getWorkspace(), cantBeDeleted);
        boolean success = cantBeDeleted.isEmpty();

        if (success) {
            new Thread(() -> {  // Access index in background so user does not have to wait for completion.
                book.getWorkspace().getSearch().unindex(SeiteSO.this);
                Logger.debug("Deleted page " + SeiteSO.this.getId() + " has been removed from search index.");
            }).start();
            new SubscriptionService().pageDeleted(seite.getId());
        }
        
        book.getWorkspace().pull(); // Datenstruktur neu aufbauen (auch wenn cantBeDeleted nicht leer ist)
        
        if (success) {
            Logger.info("Delete page " + getId() + " complete.");
        } else {
            Logger.error("Tried to delete page " + getId() + ": These files could not be deleted:"
                    + cantBeDeleted.stream().map(i -> "\n  - " + i).collect(Collectors.joining())
                    + "\n  Please delete these files manually.");
            throw new UserMessage("cantDeleteAllFiles", book.getWorkspace(), msg -> msg.replace("$id", getId()));
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
            SeiteSO parentSeite = book.seiteById(parentId); // soll auch prüfen, ob parentId gültig ist
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
        saveMeta(new CommitMessage(this, "page moved"));
        book.getWorkspace().pull();
        new SubscriptionService().pagesChanged();
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
        dao().moveFiles(files, new CommitMessage(this, "moved to book " + targetBookFolder), workspace);
        
        workspace.pull();
        // not allowed in customer version -> new SubscriptionService().pagesChanged();
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

    public void saveAll(NlsString newTitle, NlsString newContent, int version, String comment, List<String> langs, long start) {
        validate(newTitle, version, langs);
        if (content == null) {
            content = new NlsString();
        }
        for (String lang : langs) {
            seite.getTitle().setString(lang, newTitle.getString(lang));
            content.setString(lang, newContent.getString(lang));
        }
        IPageChangeStrategy strat = MinervaWebapp.factory().getPageChangeStrategy();
        strat.set(comment, this);
        CommitMessage commitMessage = strat.getCommitMessage(comment, this);
        
        Map<String, String> files = new HashMap<>();
        saveMetaTo(files);
        saveHtmlTo(files, langs);
        images.forEach(filename -> files.put(filenameImage(filename), IMAGE));
        
        dao().saveFiles(files, commitMessage, book.getWorkspace());
        
        informSubscriptionService(newContent, langs);
        
        neu = false;
        images.clear();

        if (!hasParent()) {
            // Wenn book.sorted=true ist und ein Seitentitel geändert worden ist, muss neu sortiert werden.
            book.getSeiten().sort();
        }

        reindex();
        
        new Thread(() -> new NotifyWatchers(this).notifyWatchers()).start();

        Logger.info(book.getWorkspace().getUser().getLogin() + " | " + book.getWorkspace().getBranch() + " | "
                + newTitle.getString(langs.get(0))
                + " -> Page #" + getId() + " saved. " + (System.currentTimeMillis() - start) + "ms");
    }

    private void validate(NlsString newTitle, int version, List<String> langs) {
        if (getSeite().getVersion() != version) {
            throw new UserMessage("error.simultaneousEditing", book.getWorkspace());
        }
        for (String lang : langs) {
            if (StringService.isNullOrEmpty(newTitle.getString(lang))) {
                throw new UserMessage("error.enterTitle", book.getWorkspace());
            }
        }
    }
    
    private void informSubscriptionService(NlsString newContent, List<String> langs) {
        SubscriptionService ss = new SubscriptionService();
        if (neu) {
            ss.pagesChanged();
        } else {
            TPage tpage = ss.createTPage(this, newContent, langs);
            ss.pageModified(tpage);
        }
    }
    
    public void reindex() {
        new Thread(() -> {  // Access index in background so user does not have to wait for completion.
            Logger.debug("Reindexing page #" + SeiteSO.this.getId() + "...");
            book.getWorkspace().getSearch().index(SeiteSO.this);
            Logger.debug("Page #" + SeiteSO.this.getId() + " has been reindexed.");
        }).start();
    }

    public void saveMeta(CommitMessage commitMessage) {
        Map<String, String> files = new HashMap<>();
        saveMetaTo(files);
        saveFiles(files, commitMessage);
    }

    public void saveHtml(CommitMessage commitMessage, List<String> langs) {
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
        saveFiles(files, new CommitMessage(this, "subpages reorderd"));
    }

    private void saveFiles(Map<String, String> files, CommitMessage commitMessage) {
        dao().saveFiles(files, commitMessage, book.getWorkspace());
    }

    public String filenameMeta() {
        return book.getFolder() + "/" + getId() + META_SUFFIX;
    }
    
    public String gitFilenameMeta() {
        return book.getBook().getFolder() + "/" + getId() + META_SUFFIX;
    }
    
    public String filenameHtml(String lang) {
        return book.getFolder() + "/" + lang + "/" + getId() + ".html";
    }

    public String filenameImage(String filename) {
        return book.getFolder() + "/" + filename;
    }

    private DirAccess dao() {
        return book.dao();
    }
    
    public PageChange getLastChange() {
        List<PageChange> changes = seite.getChanges();
        return changes.isEmpty() ? null : changes.get(changes.size() - 1);
    }

    /**
     * @param langs -
     * @return all pages that contain a link to this page
     */
    public List<SeiteSO> linksTo(List<String> langs) {
        return book.getSeiten().findLink(getId(), langs);
    }
    
    /**
     * Page has been modified. Inform online help about it.
     */
    public void updateOnlineHelp() {
        SubscriptionService ss = new SubscriptionService();
        TPage tpage = ss.createTPage(this, getContent(), MinervaWebapp.factory().getLanguages());
        ss.pageModified(tpage);
    }

    public void updateOnlineHelp_nowVisible() {
        // It's like a new page, so send all data.
        new SubscriptionService().pagesChanged(); // threading inside method
    }

    public void updateOnlineHelp_nowInvisible() {
        new SubscriptionService().pageDeleted(seite.getId());
    }
    
    public void log(String msg) {
        book.getWorkspace().getUser().log("#" + getId() + " \"" + getTitle() + "\" " + msg);
    }
    
    public SeiteVisible isVisible(String customer, String lang) {
        ExclusionsService sv = new ExclusionsService();
        sv.setCustomer(customer);
        sv.setExclusions(new Exclusions(book.getWorkspace().getExclusions().get()));
        return isVisible(sv, lang);
    }
    
    public SeiteVisible isVisible(ExclusionsService sv, String lang) {
        return new SeiteVisible(isSeiteVisible(sv, lang), sv);
    }
    
    /**
     * Don't show page if return value is below 1.
     * @param seite page
     * @return 0: has no content, -1: not accessible, -2: not visible, 1, 2 or 3: show page (see SeiteSO.hasContent)
     */
    private int isSeiteVisible(ExclusionsService exclusionsService, String lang) {
        int c = hasContent(lang);
        if (c > 0) {
            exclusionsService.setSeite(this);
            if (!exclusionsService.isAccessible()) {
                return -1;
            } else if (seite.getTags().contains("invisible")) {
                return -2;
            }
        }
        return c;
    }

    /**
     * has content: > 0, has no content: 0
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
    
    /**
     * Books instance have been changed after pull.
     * @return same page but as fresh instance from fresh book instance
     */
    public SeiteSO getMeAsFreshInstance() {
        return book.getMeAsFreshInstance().seiteById(getId());
    }
    
    public TocMacroPage getTocMacroPage() {
        return getTocMacroPage(true);
    }
    
    public TocMacroPage getTocMacroPage(boolean withSubpagesLevels) {
        return new TocMacroPage() {

            @Override
            public String getId() {
                return getSeite().getId();
            }

            @Override
            public String getTitle(String lang) {
                return getSeite().getTitle().getString(lang);
            }
            
            @Override
            public Set<String> getTags() {
                return getSeite().getTags();
            }

            @Override
            public int getTocHeadingsLevels() {
                return getSeite().getTocHeadingsLevels();
            }

            @Override
            public int getTocSubpagesLevels() {
                return withSubpagesLevels ? getSeite().getTocSubpagesLevels() : 0;
            }
            
            @Override
            public boolean isVisible(String customer, String lang) {
                return SeiteSO.this.isVisible(customer, lang).isVisible();
            }
            
            @Override
            public List<TocMacroPage> getSubpages(String lang) {
                List<TocMacroPage> ret = new ArrayList<>();
                for (SeiteSO seite : getSeiten(lang)) {
                    ret.add(seite.getTocMacroPage(withSubpagesLevels));
                }
                return ret;
            }
        };
    }
}
