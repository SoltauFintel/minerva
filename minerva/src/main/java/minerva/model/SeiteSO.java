package minerva.model;

import static gitper.access.DirAccess.IMAGE;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.FileService;
import gitper.base.StringService;
import gitper.movefile.ChangeFile;
import gitper.movefile.IMoveFile;
import gitper.movefile.MoveFile;
import minerva.MinervaWebapp;
import minerva.base.MinervaMetrics;
import minerva.base.NlsString;
import minerva.base.TextService;
import minerva.base.UserMessage;
import minerva.comment.SeiteCommentService2;
import minerva.exclusions.SeiteSichtbar;
import minerva.seite.AlleSeiten;
import minerva.seite.IPageChangeStrategy;
import minerva.seite.ISeite;
import minerva.seite.PageChange;
import minerva.seite.Seite;
import minerva.seite.WatchersService;
import minerva.seite.link.ExtractLinksContext;
import minerva.subscription.SubscriptionService;
import minerva.subscription.TPage;
import minerva.user.CustomerMode;
import minerva.user.User;
import minerva.validate.RemoveStyleAttributesService;
import ohhtml.toc.HelpKeysForHeading;
import ohhtml.toc.TocMacroPage;

public class SeiteSO implements ISeite, Comparable<SeiteSO> {
    public static final String META_SUFFIX = ".meta";
    public static final String ROOT_ID = "root";
    private final BookSO book;
    private Seite seite;
    /** Unterseiten */
    private SeitenSO seiten;
    /** Snapshot of image files before editing */
    private final List<String> imagesBefore = new ArrayList<>();

    /** null: nicht geladen */
    private NlsString content = null;
    /** true: it's a new page that has never been saved, false: already existing page */
    private boolean neu = false;
    
    /**
     * Loaded Seite constructor
     * @param book -
     * @param seite -
     * @param alleSeiten -
     */
    public SeiteSO(BookSO book, Seite seite, AlleSeiten alleSeiten) {
        if (seite == null) {
            throw new IllegalArgumentException("seite is null");
        }
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
        this.imagesBefore.addAll(c.imagesBefore);
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
        return editSort(getTitle());
    }

    public String getSortTitle(String lang) {
        return editSort(getSeite().getTitle().getString(lang));
    }
    
    private String editSort(String sort) {
        sort = StringService.umlaute(sort);
        if (isFeatureTree()) {
            if (isPageInFeatureTree()) {
                sort = "1" + sort; // put normal pages at begin
            } else {
                sort = "2" + sort; // put features at end
            }
        }
        return sort;
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
        Seite s = (Seite) MinervaWebapp.factory().getBackendService().forceReloadIfCheap(filenameMeta());
        if (s == null) { // any problem, then just cancel
            return;
        }
        seite = s;
        content = null; // force reload
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

    public List<String> getImagesBefore() {
        return imagesBefore;
    }

    public void activateSorted() {
        seite.setSorted(true);
        saveMeta(commitMessage("subpages sorted alphabetically"));
        book.getWorkspace().pull(); // ja, ist etwas brutal...
        new SubscriptionService().pagesChanged();
    }

    public TagsSO tags() {
        return new TagsSO(this);
    }
    
    public void remove() {
        Set<String> filenamesToDelete = new HashSet<>();
        List<SeiteSO> changedPages = new ArrayList<>();
        remove(filenamesToDelete, changedPages);

        crossBookLinks_persistChangedPages(filenamesToDelete, changedPages);
        
        List<String> cantBeDeleted = new ArrayList<>();
        dao().deleteFiles(filenamesToDelete, commitMessage("page deleted"), book.getWorkspace(), cantBeDeleted);
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

	public void remove(Set<String> filenamesToDelete, List<SeiteSO> changedPages) {
        // Untergeordnete Seiten
        for (SeiteSO unterseite : seiten) {
            unterseite.remove(filenamesToDelete, changedPages); // rekursiv
        }
        
        String bookFolder = book.getFolder();
        String r = getId() + "/*";
        
        removeCrossBookLinksTargetingThisPage(changedPages);
        
        // Images für diese Seite
		filenamesToDelete.add(bookFolder + "/img/" + r);

        // Feature tree Daten für diese Seite
		filenamesToDelete.add(bookFolder + "/feature-fields/" + getId() + ".ff");

        // Kommentare (inkl. Images) dieser Seite
        filenamesToDelete.add(new SeiteCommentService2(this).dir() + "/**");
        
		// HTML-Seiteninhalte aller Sprachen für diese Seite
		MinervaWebapp.factory().getLanguages().forEach(lang -> {
			String dn = filenameHtml(lang);
			if (new File(dn).isFile()) {
				filenamesToDelete.add(dn);
			}
		});
        
        // Metadaten dieser Seite
        filenamesToDelete.add(filenameMeta());
    }
    
    private void removeCrossBookLinksTargetingThisPage(List<SeiteSO> changedPages) {
        String x = book.getBook().getFolder() + "/" + getId();
        for (BookSO b : book.getWorkspace().getBooks()) {
            for (SeiteSO s : b.getAlleSeiten()) {
                if (s.getSeite().getLinks().removeIf(i -> i.equals(x))) {
                    String xId = s.getId();
                    boolean found = false;
                    for (SeiteSO j : changedPages) {
                        if (j.getId().equals(xId)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        changedPages.add(s);
                    }
                }
            }
        }
    }
    
    public void crossBookLinks_persistChangedPages(Set<String> filenamesToDelete, List<SeiteSO> changedPages) {
        changedPages.removeIf(s -> filenamesToDelete.contains(s.filenameMeta()));
        if (!changedPages.isEmpty()) {
            Map<String, String> files = new HashMap<>();
            for (SeiteSO s : changedPages) {
                s.saveMetaTo(files);
            }
            dao().saveFiles(files, commitMessage("page deleted (cross-book links cleanup)"), book.getWorkspace());
        }
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
        saveMeta(commitMessage("page moved"));
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
        dao().moveFiles(files, commitMessage("moved to book " + targetBookFolder), workspace);
        
        workspace.pull();
        // not allowed in customer version -> new SubscriptionService().pagesChanged();
    }
    
    private void changePageTo(int newPosition, List<IMoveFile> files) {
        seite.setParentId(ROOT_ID);
        seite.setPosition(newPosition);
        files.add(new ChangeFile(filenameMeta(), FileService.prettyJSON(seite)));
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
    
    public String getLogLine(User u) {
    	if (u == null) {
    		u = book.getWorkspace().getUser().getUser();
    	}
		return u.getLogin() + " | " + book.getWorkspace().getBranch() + " | " + book.getBook().getFolder() + " | " + getTitle();
    }

    public String duplicate(List<String> langs, boolean completeCopy) {
    	// create new page
        long start = System.currentTimeMillis();
    	String id;
    	if (hasParent()) {
            SeiteSO parent = getParent();
            id = parent.getSeiten().createSeite(parent, book, book.dao());
    	} else {
    		id = book.createTopLevelSeite();
    	}
    	Logger.info(getLogLine(null) + " | Page " + id + " is a copy.");
    	
    	// copy data
        SeiteSO copy = book._seiteById(id);
        copy.getSeite().copyFrom(seite, completeCopy);
        copy.content = new NlsString();
        for (String lang : langs) {
        	// find new title
    		List<String> allPageTitles = book.getAlleSeiten().stream()
    				.map(s -> s.getSeite().getTitle().getString(lang))
    				.collect(Collectors.toList());
    		String newTitle = TextService.findCopyOfTitle(seite.getTitle().getString(lang), lang, allPageTitles);
        	copy.getSeite().getTitle().setString(lang, newTitle);
        	
        	// copy HTML
        	copy.content/*not getContent()!*/.setString(lang, content.getString(lang)
        			.replace("img/" + seite.getId() + "/", "img/" + id + "/")); // adjust image paths
		}

        // copy images
        book.dao().copyFiles(book.getFolder(), "/img/" + seite.getId(), "/img/" + id);
        
        // save page (needed if there are images, but we save it always to have same behaviour)
        copy.saveAll(copy.getSeite().getTitle(), copy.getContent(), copy.getSeite().getVersion(), "duplicate", langs, start);
        
    	return id;
    }

    public void saveAll(NlsString newTitle, NlsString newContent, int version, String comment, List<String> langs, long start) {
        validate(newTitle, newContent, version, langs);
        if (content == null) {
            content = new NlsString();
        }
        Set<String> images = imagesAfterEdit();
        // Verdacht dass das was kaputt machen könnte. -> new FixHttpImage().process(newContent, langs, images, book, seite.getId());
        for (String lang : langs) {
            seite.getTitle().setString(lang, newTitle.getString(lang));
            String html = newContent.getString(lang);
            html = new RemoveStyleAttributesService().filter(html, null, null);
            content.setString(lang, html);
        }
        comment = createTagsFromComment(comment);
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

        if (!hasParent()) {
            // Wenn book.sorted=true ist und ein Seitentitel geändert worden ist, muss neu sortiert werden.
            book.getSeiten().sort();
        }

        reindex();
        
        new Thread(() -> new WatchersService(this).notifyWatchers()).start();

        long duration = System.currentTimeMillis() - start;
		Logger.info(getLogLine(null) + " | *** Page saved. (#" + getId() + ", " + duration + "ms)");
		MinervaMetrics.PAGE_SAVETIME.record(duration);
		Logger.debug("Metric PAGE_SAVETIME sent: " + duration);
    }

    private String createTagsFromComment(String comment) {
        for (String i : List.of("tag=", "tags=")) {
            if (comment.startsWith(i)) {
                for (String tag : comment.substring(i.length()).split(",")) {
                    if (!tag.isBlank()) {
                        seite.getTags().add(tag.trim().toLowerCase());
                    }
                }
                return "";
            }
        }
        return comment;
    }

    private void validate(NlsString newTitle, NlsString newContent, int version, List<String> langs) {
        if (getSeite().getVersion() != version) {
            book.getUser().getJournal().save(book.getWorkspace().getBranch(), getId(), newTitle, newContent);
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
        files.put(filenameMeta(), FileService.prettyJSON(seite));
    }

    public void saveHtmlTo(Map<String,String> files, List<String> langs) {
        langs.forEach(lang -> {
            String html = TextService.prettyHTML(content.getString(lang));
            content.setString(lang, html); // write it back - important for HTML editing mode
            files.put(filenameHtml(lang), html);
        });
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
        if (!files.isEmpty()) {
            saveFiles(files, commitMessage("subpages reorderd"));
        }
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
     * @return all pages that contain a link to this page (or subpage(s))
     */
    public TreeSet<SeiteSO> linksTo(List<String> langs) {
        ExtractLinksContext xlctx = new ExtractLinksContext();
        TreeSet<SeiteSO> ret = book.getSeiten().findLink(getId(), langs, xlctx);
        seiten.linksTo(langs, book, xlctx, ret);
        return ret;
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
                SeiteSichtbar ssc = new SeiteSichtbar(SeiteSO.this.book.getWorkspace(), lang);
                ssc.setCustomerMode(new CustomerMode(customer));
                return ssc.isVisible(SeiteSO.this);
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
    
    public String getFeatureTag() {
        return getFeatureTagR(this);
    }
        
    private String getFeatureTagR(SeiteSO seite) {
        for (String tag : seite.getSeite().getTags()) {
            if (tag.startsWith("ft_")) {
                return tag;
            }
        }
        if (seite.hasParent()) {
            return seite.getParent().getFeatureTag(); // recursive
        } else {
            return "ft";
        }
    }
    
    public boolean hasFt_tag() {
        for (String tag : seite.getTags()) {
            if (tag.startsWith("ft_")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isFeatureTree() {
        return book.isFeatureTree();
    }

    public boolean isInternal() {
        return book.isInternal();
    }
    
    public boolean isNotPublic() {
        return book.isNotPublic();
    }
    
    /**
     * Feature tree: Is the tag "page" set?
     * @return true: Page should be displayed as a normal content page in the feature tree,
     * false: not feature tree or normal representation as feature.
     */
    public boolean isPageInFeatureTree() {
        return book.isFeatureTree() && seite.getTags().contains("page");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SeiteSO other = (SeiteSO) obj;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public int compareTo(SeiteSO b) {
        return getTitle().compareToIgnoreCase(b.getTitle());
    }

    /**
     * Sub features will be displayed if there's a tag show-all-pages. That is needed for the menu structure.
     * @return true: don't show sub features because they are too many
     */
    public boolean checkSubfeaturesLimit() {
    	return !seite.getTags().contains("show-all-pages") && getSeiten().size() > MinervaWebapp.factory().getConfig().getMaxSubfeatures();
    }
    
    public String viewlink() {
		return "/s/" + Escaper.esc(book.getWorkspace().getBranch()) + "/"
				+ Escaper.esc(book.getBook().getFolder()) + "/" + seite.getId();
	}

    public void imagesBeforeEdit() {
        Set<String> filenames = getImageFilenames();
        imagesBefore.clear();
        if (filenames != null) {
            imagesBefore.addAll(filenames);
        }
        Logger.debug("images before: " + imagesBefore);
    }

    public Set<String> imagesAfterEdit() {
        Set<String> ret = new TreeSet<>();
        Set<String> filenames = getImageFilenames();
        if (filenames != null) {
            filenames.removeAll(imagesBefore);
            Logger.debug("images delta: " + filenames);
            String prefix = "img/" + seite.getId() + "/";
            filenames.forEach(dn -> ret.add(prefix + dn));
        }
        imagesBefore.clear();
        return ret;
    }

    public Set<String> getImageFilenames() {
        String imageDir = book.getFolder() + "/img/" + seite.getId();
        return book.dao().getFilenames(imageDir);
    }
    
    public List<String> getHeadingHelpKeys(String lang, String headingTitle) {
        if (seite.getHkh() != null) {
            for (HelpKeysForHeading i : seite.getHkh()) {
                if (i.getLanguage().equals(lang) && i.getHeading().equals(headingTitle)) {
                    return i.getHelpKeys();
                }
            }
        }
        return new ArrayList<>();
    }
    
    public void saveHeadingHelpKeys(String lang, String headingTitle, String helpKeysText) {
        List<String> helpKeys = new ArrayList<>();
        splitHelpKeys(helpKeysText.replace(",", "\n"), helpKeys);
        if (helpKeys.isEmpty()) {
            if (seite.getHkh() == null) {
                return;
            }
            seite.getHkh().removeIf(i -> i.getLanguage().equals(lang) && i.getHeading().equals(headingTitle));
            if (seite.getHkh().isEmpty()) {
                seite.setHkh(null);
            }
        } else {
            if (seite.getHkh() == null) {
                seite.setHkh(new ArrayList<>());
            }
            boolean found = false;
            for (HelpKeysForHeading i : seite.getHkh()) {
                if (i.getLanguage().equals(lang) && i.getHeading().equals(headingTitle)) {
                    if (i.getHelpKeys().equals(helpKeys)) {
                        return;
                    }
                    i.setHelpKeys(helpKeys);
                    found = true;
                }
            }
            if (!found) {
                HelpKeysForHeading i = new HelpKeysForHeading();
                i.setLanguage(lang);
                i.setHeading(headingTitle);
                i.setHelpKeys(helpKeys);
                seite.getHkh().add(i);
            }
        }
        saveMeta(commitMessage("help keys for headings"));
        updateOnlineHelp();
    }
    
    public void saveHelpKeys(String helpKeysText) {
        seite.getHelpKeys().clear();
        splitHelpKeys(helpKeysText, seite.getHelpKeys());
        saveMeta(commitMessage("help keys"));
        updateOnlineHelp();
    }
    
    public static void splitHelpKeys(String helpKeysText, List<String> helpKeysTarget) {
        for (String line : helpKeysText.split("\n")) {
            String helpKey = line.trim();
            if (!helpKey.isEmpty()) {
                helpKeysTarget.add(helpKey);
            }
        }
        Collections.sort(helpKeysTarget);
    }
    
    public boolean hasAttachments() {
        return new AttachmentsSO(this).hasAttachments();
    }
    
    public boolean isCustomerMode() {
        return book.getWorkspace().getUser().getCustomerMode().isActive();
    }
    
	public CommitMessage commitMessage(String comment) {
		String title = seite.getTitle().getString(MinervaWebapp.factory().getLanguages().get(0));
		if (!title.equals(comment) && !comment.isEmpty()) {
			title += ": " + comment;
		}
		return new CommitMessage(title);
	}
}
