package minerva.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.spark.Context;
import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.base.MinervaMetrics;
import minerva.base.UserMessage;
import minerva.book.Book;
import minerva.book.BookType;
import minerva.exclusions.SeiteSichtbar;
import minerva.search.BookFilter;
import minerva.seite.AlleSeiten;
import minerva.seite.Breadcrumb;
import minerva.seite.IBreadcrumbLinkBuilder;
import minerva.seite.ISeite;
import minerva.seite.tag.TagNList;
import minerva.subscription.SubscriptionService;

public class BookSO implements BookFilter {
    private static final Object LOCK = new Object();
    public static final String BOOK_PREFIX = "book:";
    private final WorkspaceSO workspace;
    private final Book book;
    /** Seiten auf oberster Ebene */
    private SeitenSO seiten; // use only in _seiten() and in savePagesAfterReordering()
    
    public BookSO(WorkspaceSO workspace, Book book) {
        this.workspace = workspace;
        this.book = book;
    }
    
    public static BookSO retrieve(Context ctx) {
        UserSO user = StatesSO.get(ctx).getUser();
        WorkspaceSO workspace = user.getWorkspace(ctx.pathParam("branch"));
        return workspace.getBooks().byFolder(ctx.pathParam("book"));
    }
    
    // SeitenSO lazy laden
    private SeitenSO _seiten() {
        synchronized (LOCK) {
            if (seiten == null) {
                // Alle Seiten eines Buchs laden
                Map<String, String> files = workspace.dao().loadAllFiles(workspace.getFolder() + "/" + book.getFolder());
                AlleSeiten alleSeiten = new AlleSeiten(files);
                int n = files.size();
                MinervaMetrics.PAGE_LOADED.add(n);
                seiten = SeitenSO.findeUnterseiten(getISeite(), alleSeiten, this);
            }
            return seiten;
        }
    }
    
    // public for migration
    public ISeite getISeite() {
        return new ISeite() {
            @Override
            public String getId() {
                return SeiteSO.ROOT_ID;
            }

            @Override
            public boolean isSorted() {
                return book.isSorted();
            }
            
            @Override
            public boolean isReversedOrder() {
                return false;
            }

            @Override
            public String getTitle() {
                return book.getTitle().getString(getUser().getPageLanguage());
            }
            
            @Override
            public SeitenSO getSeiten() {
                return _seiten();
            }
        };
    }

    public SeitenSO getSeiten() {
        return _seiten();
    }

    public SeitenSO getSeiten(String lang) {
        var ret = _seiten();
        ret.sort(lang);
        return ret;
    }

    /**
     * Use this method to get all pages and for writing an algo without recursion (if recursion isn't needed).
     * @return all pages
     */
    public List<SeiteSO> getAlleSeiten() {
        List<SeiteSO> ret = new ArrayList<>();
        addSeiten(_seiten(), ret);
        return ret;
    }

    private void addSeiten(SeitenSO seiten, List<SeiteSO> result) {
        for (SeiteSO seite : seiten) {
            result.add(seite);
            addSeiten(seite.getSeiten(), result); // recursive
        }
    }
    
    public SeiteSO seiteById(String id) {
        return _seiten().byId(id);
    }
    
    public SeiteSO _seiteById(String id) {
        return _seiten()._byId(id);
    }

    public Book getBook() {
        return book;
    }
    
    /**
     * @return Attention: this is the absolute path! Use getBook().getFolder() for just getting the folder name.
     */
    public String getFolder() {
        return workspace.getFolder() + "/" + book.getFolder();
    }
    
    public DirAccess dao() {
        return workspace.dao();
    }

    public WorkspaceSO getWorkspace() {
        return workspace;
    }

    public UserSO getUser() {
        return workspace.getUser();
    }
    
    public String getUserRealName() {
        return workspace.getUser().getUser().getRealName();
    }
    
    /**
     * @return page ID
     */
    public String createTopLevelSeite() {
        return _seiten().createSeite(getISeite(), this, dao());
    }
    
    @Override
    public String getTitle() {
        return book.getTitle().getString(getUser().getGuiLanguage());
    }
    
    @Override
    public String getBookFilterId() {
        return BOOK_PREFIX + book.getFolder();
    }
    
    public boolean isFeatureTree() {
        return BookType.FEATURE_TREE.equals(book.getType());
    }

    public boolean isInternal() {
        return BookType.INTERNAL.equals(book.getType());
    }

    public boolean isNotPublic() {
        return !book.getType().isPublic();
    }
    
    public boolean isReleaseNotes() {
        return BookType.RELEASE_NOTES.equals(book.getType());
    }

    public void activateSorted() {
        book.setSorted(true);
        saveBook();
        new SubscriptionService().pagesChanged();
    }
    
    private void saveBook() {
        BooksSO books = workspace.getBooks();
        books.incVersion();
        books.save(cm("alphabetical sorting enabled"));
        workspace.pull(); // ja, ist etwas brutal...
    }

    // similar method in SeiteSO
    public void savePagesAfterReordering(SeitenSO reorderdSeiten) {
        synchronized (BOOK_PREFIX) {
            this.seiten = reorderdSeiten;
        }
        Map<String, String> files = new HashMap<>();
        if (book.isSorted()) {
            book.setSorted(false);
            workspace.getBooks().saveTo(files);
        }
        reorderdSeiten.setPositionsAndSaveTo(files);
        if (!files.isEmpty()) {
            dao().saveFiles(files, cm("subpages reorderd"), workspace);
        }
    }

    /**
     * recursive
     * @param tag -
     * @return pages
     */
    public List<SeiteSO> findTag(String tag) {
        List<SeiteSO> ret = new ArrayList<>();
        for (SeiteSO seite : _seiten()) {
            ret.addAll(seite.tags().findTag(tag));
        }
        return ret;
    }

    public void addAllTags(TagNList tags) {
        _seiten().forEach(seite -> seite.tags().addAllTags(tags));
    }

    public List<Breadcrumb> getBreadcrumbs(String seiteId, IBreadcrumbLinkBuilder builder) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        if (_seiten().getBreadcrumbs(seiteId, builder, breadcrumbs)) {
            Breadcrumb b = new Breadcrumb();
            b.setTitle(book.getTitle());
            b.setLink(builder.build(workspace.getBranch(), book.getFolder(), null));
            breadcrumbs.add(b);
        }
        return breadcrumbs;
    }

    public boolean hasContent(SeiteSichtbar ssc) {
        for (SeiteSO seite : _seiten()) {
            if (ssc.isVisible(seite)) {
                return true;
            }
        }
        return false;
    }
    
    public CommitMessage cm(String comment) {
        return new CommitMessage(book.getFolder() + ": " + comment);
    }
    
    public SeiteSO getLastChange() {
        return getSeiten().getLastChange();
    }
    
    /**
     * Books instance have been changed after pull.
     * @return same book but as fresh instance from fresh books instance
     */
    public BookSO getMeAsFreshInstance() {
        return workspace.getBooks().byFolder(book.getFolder());
    }

    public Set<String> getAllSeiteIdSet() {
        Set<String> set = new HashSet<>();
        fillSet(_seiten(), set);
        return set;
    }
    
    private void fillSet(SeitenSO seiten, Set<String> set) {
        for (SeiteSO seite : seiten) {
            set.add(seite.getId());
            fillSet(seite.getSeiten(), set); // recursive
        }
    }
    
    public TreeSet<String> loadMultiSelect() {
        TreeSet<String> selectedPages = new TreeSet<>();
        String content = FileService.loadPlainTextFile(getMultiSelectFile());
        if (content != null) {
            for (String i : content.split(",")) {
                selectedPages.add(i);
            }
        }
        return selectedPages;
    }

    public void saveMultiSelect(String id, boolean checked) {
        var selectedPages = loadMultiSelect();
        if (checked) {
            selectedPages.add(id);
        } else {
            selectedPages.remove(id);
        }
        _saveMultiSelect(selectedPages);
    }
    
    public void saveMultiSelectByTag(String tag) {
        var selectedPages = loadMultiSelect();
        for (SeiteSO seite : findTag(tag)) {
            selectedPages.add(seite.getId());
        }
        _saveMultiSelect(selectedPages);
    }
    
    private void _saveMultiSelect(TreeSet<String> selectedPages) {
        FileService.savePlainTextFile(getMultiSelectFile(),
                selectedPages.isEmpty() ? null : selectedPages.stream().collect(Collectors.joining(",")));
    }

    public void clearMultiSelect() {
        FileService.savePlainTextFile(getMultiSelectFile(), null); // löscht Datei
    }
    
    private File getMultiSelectFile() {
        return new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder(),
                getUser().getLogin() + "-" + book.getFolder() + ".ms");
    }

    public void multiSelectAction(Set<String> selectedPages, String tag, boolean clear, boolean add) {
        if (selectedPages.isEmpty()) {
            throw new UserMessage("multiSelectError1", getUser());
        } else if (!clear && StringService.isNullOrEmpty(tag)) {
            throw new UserMessage("multiSelectError2", getUser());
        } else if (clear && !StringService.isNullOrEmpty(tag)) { // Bedienungsschutz
            throw new UserMessage("multiSelectError3", getUser());
        }
        
        Map<String, String> files = new HashMap<>();
        for (String seiteId : selectedPages) {
            SeiteSO seite = _seiteById(seiteId);
            if (seite != null) {
                if (multiSelectAction_editTags(seite, tag, clear, add)) {
                    seite.saveMetaTo(files);
                }
            } // else: Seite gibt es nicht mehr
        }
        String cm = "clear tags";
        if (!clear) {
            cm = (add ? "add" : "delete") + " tag: " + tag.trim().toLowerCase();
        }
        dao().saveFiles(files, new CommitMessage(cm), workspace);
    }

    private boolean multiSelectAction_editTags(SeiteSO seite, String pTags, boolean clear, boolean add) {
        boolean dirty = false;
        Set<String> tags = seite.getSeite().getTags();
        if (clear) {
            if (!tags.isEmpty()) {
                tags.clear();
                dirty = true;
            }
        } else {
            for (String tag : pTags.split(",")) {
                tag = tag.trim().toLowerCase();
                if (add) {
                    if (tags.add(tag)) {
                        dirty = true;
                    }
                } else { // delete
                    if (tags.remove(tag)) {
                        dirty = true;
                    }
                }
            }
        }
        return dirty;
    }
}
