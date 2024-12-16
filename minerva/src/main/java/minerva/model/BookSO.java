package minerva.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import gitper.access.CommitMessage;
import gitper.access.DirAccess;
import minerva.book.Book;
import minerva.book.BookType;
import minerva.exclusions.SeiteSichtbar;
import minerva.seite.Breadcrumb;
import minerva.seite.IBreadcrumbLinkBuilder;
import minerva.seite.ISeite;
import minerva.seite.Seite;
import minerva.seite.tag.TagNList;
import minerva.subscription.SubscriptionService;

public class BookSO {
    private final WorkspaceSO workspace;
    private final Book book;
    /** Seiten auf oberster Ebene */
    private SeitenSO seiten;
    
    public BookSO(WorkspaceSO workspace, Book book) {
        this.workspace = workspace;
        this.book = book;
        
        // Alle Seiten eines Buchs laden
        Map<String, String> files = workspace.dao().loadAllFiles(workspace.getFolder() + "/" + book.getFolder());
        Gson gson = new Gson();
        List<Seite> alleSeiten = files.entrySet().stream()
                .filter(e -> e.getKey().endsWith(SeiteSO.META_SUFFIX))
                .map(e -> gson.fromJson(e.getValue(), Seite.class))
                .collect(Collectors.toList());

        seiten = SeitenSO.findeUnterseiten(getISeite(), alleSeiten, this);
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
                return seiten;
            }
        };
    }

    public SeitenSO getSeiten() {
        return seiten;
    }

    public SeitenSO getSeiten(String lang) {
        seiten.sort(lang);
        return seiten;
    }

    /**
     * Use this method to get all pages and for writing an algo without recursion (if recursion isn't needed).
     * @return all pages
     */
    public List<SeiteSO> getAlleSeiten() {
        List<SeiteSO> ret = new ArrayList<>();
        addSeiten(seiten, ret);
        return ret;
    }

    private void addSeiten(SeitenSO seiten, List<SeiteSO> result) {
        for (SeiteSO seite : seiten) {
            result.add(seite);
            addSeiten(seite.getSeiten(), result); // recursive
        }
    }
    
    public SeiteSO seiteById(String id) {
        return seiten.byId(id);
    }
    
    public SeiteSO _seiteById(String id) {
        return seiten._byId(id);
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
        return seiten.createSeite(getISeite(), this, dao());
    }
    
    public String getTitle() {
        return book.getTitle().getString(getUser().getGuiLanguage());
    }
    
    public boolean isFeatureTree() {
        return BookType.FEATURE_TREE.equals(book.getType());
    }

    public boolean isInternal() {
        return BookType.INTERNAL.equals(book.getType());
    }

    public boolean isNotPublic() {
        return !BookType.PUBLIC.equals(book.getType());
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
        this.seiten = reorderdSeiten;
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
        for (SeiteSO seite : seiten) {
            ret.addAll(seite.tags().findTag(tag));
        }
        return ret;
    }

    public void addAllTags(TagNList tags) {
        seiten.forEach(seite -> seite.tags().addAllTags(tags));
    }

    public List<Breadcrumb> getBreadcrumbs(String seiteId, IBreadcrumbLinkBuilder builder) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        if (seiten.getBreadcrumbs(seiteId, builder, breadcrumbs)) {
            Breadcrumb b = new Breadcrumb();
            b.setTitle(book.getTitle());
            b.setLink(builder.build(workspace.getBranch(), book.getFolder(), null));
            breadcrumbs.add(b);
        }
        return breadcrumbs;
    }

    public boolean hasContent(SeiteSichtbar ssc) {
        for (SeiteSO seite : seiten) {
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
    	fillSet(seiten, set);
    	return set;
    }
    
    private void fillSet(SeitenSO seiten, Set<String> set) {
		for (SeiteSO seite : seiten) {
			set.add(seite.getId());
			fillSet(seite.getSeiten(), set); // recursive
		}
	}
}
