package minerva.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.MList;
import minerva.comment.Comment;
import minerva.comment.SeiteCommentService;
import minerva.seite.Breadcrumb;
import minerva.seite.IBreadcrumbLinkBuilder;
import minerva.seite.CommentWithSeite;
import minerva.seite.PageChange;
import minerva.seite.Seite;
import minerva.seite.TreeItem;
import minerva.seite.link.ExtractLinksContext;

public class SeitenSO extends MList<SeiteSO> {

    public SeitenSO(ISeite parent) {
        super(getComparator(parent));
    }

    private static Comparator<SeiteSO> getComparator(ISeite parent) {
        if (parent.isSorted()) {
            return new SeiteTitleComparator(parent.isReversedOrder());
        } else {
            return (a, b) -> Integer.valueOf(a.getSeite().getPosition())
                    .compareTo(Integer.valueOf(b.getSeite().getPosition()));
        }
    }

    public static SeitenSO findeUnterseiten(ISeite parent, List<Seite> alleSeiten, BookSO book) {
        SeitenSO ret = new SeitenSO(parent);
        for (Seite seite : alleSeiten) {
            if (seite.getParentId().equals(parent.getId())) {
                ret.add(new SeiteSO(book, seite, alleSeiten));
            }
        }
        return ret;
    }

    /**
     * @param id -
     * @return Exception if Seite not found
     */
    public SeiteSO byId(String id) {
        SeiteSO ret = _byId(id);
        if (ret == null) {
            Logger.error("Page not found: " + id);
            throw new RuntimeException("This page does not exist.");
        }
        return ret;
    }

    /**
     * @param id -
     * @return null if Seite not found
     */
    public SeiteSO _byId(String id) {
        for (SeiteSO seite : this) {
            if (seite.getId().equals(id)) {
                return seite;
            }
            SeiteSO x = seite.getSeiten()._byId(id); // recursive
            if (x != null) {
                return x;
            }
        }
        return null;
    }

    /**
     * not recursive
     * @param tag -
     * @return null if not found
     */
    public SeiteSO _byTag(String tag) {
        for (SeiteSO seite : this) {
            if (seite.getSeite().getTags().contains(tag)) {
                return seite;
            }
        }
        return null;
    }

    /**
     * not recursive
     * @param title -
     * @param lang -
     * @return null if not found
     */
    public SeiteSO _byTitle(String title, String lang) {
        for (SeiteSO seite : this) {
            if (seite.getSeite().getTitle().getString(lang).equals(title)) {
                return seite;
            }
        }
        return null;
    }

    /**
     * Just create new SeiteSO. Does not save object.
     * @param parent -
     * @param book -
     * @return page ID
     */
    public String createSeite(ISeite parent, BookSO book, DirAccess dao) {
        SeiteSO neueSeite = createSeite(parent, book, IdGenerator.createId6());
        
        // Check if page already exists (ID collision) (should never happen)
        if (new MultiPurposeDirAccess(dao).load(neueSeite.filenameMeta()) != null) {
            Logger.error("createSeite error: file already exists: " + neueSeite.filenameMeta());
            throw new RuntimeException("File already exists! Please try again.");
        }
        
        return neueSeite.getId();
    }

    /**
     * directly called for migration, otherwise use other createSeite() method
     * @param parent -
     * @param book -
     * @param newId -
     * @return SeiteSO
     */
    public SeiteSO createSeite(ISeite parent, BookSO book, String newId) {
        Seite neueSeite = new Seite();
        neueSeite.setId(newId);
        neueSeite.setParentId(parent.getId());
        neueSeite.setPosition(calculateNextPosition());
        MinervaWebapp.factory().setNewSeiteTitle(neueSeite.getTitle(), "" + neueSeite.getPosition());
        
        SeiteSO ret = new SeiteSO(book, neueSeite, parent);
        ret.setNeu(true);
        add(ret);
        return ret;
    }

    public int calculateNextPosition() {
        int max = 0;
        for (SeiteSO seite : this) {
            int position = seite.getSeite().getPosition();
            if (position > max) {
                max = position;
            }
        }
        return max + 1;
    }
    
    public void setPositionsAndSaveTo(Map<String, String> files) {
        int position = 1;
        for (SeiteSO sub : this) {
            sub.getSeite().setPosition(position++);
            sub.saveMetaTo(files);
        }
    }

    public List<SeiteSO> searchInTitle(String search, String excludeSeiteId, List<String> langs) {
        List<SeiteSO> ret = new ArrayList<>();
        for (SeiteSO seite : this) {
            if (!seite.getId().equals(excludeSeiteId)) {
                for (String lang : langs) {
                    if (seite.getSeite().getTitle().getString(lang).toLowerCase().contains(search)) {
                        ret.add(seite);
                        break; // add page only once
                    }
                }
            }
            ret.addAll(seite.getSeiten().searchInTitle(search, excludeSeiteId, langs));
        }
        return ret;
    }
    
    public boolean getBreadcrumbs(String seiteId, IBreadcrumbLinkBuilder builder, List<Breadcrumb> breadcrumbs) {
        for (SeiteSO seite : this) {
            if (seite.getId().equals(seiteId)) {
                return true; // gefunden, Ende der Suche
            }
            if (seite.getSeiten().getBreadcrumbs(seiteId, builder, breadcrumbs)) { // recursive
                Breadcrumb b = new Breadcrumb();
                b.setTitle(seite.getSeite().getTitle());
                b.setLink(builder.build(seite.getBook().getWorkspace().getBranch(),
                        seite.getBook().getBook().getFolder(),
                        seite.getId()));
                breadcrumbs.add(b);
                return true;
            }
        }
        return false;
    }
    
    public void onlyRemove(SeiteSO seite) {
        remove(seite);
    }

    public void sort(String language) {
        if (comparator instanceof SeiteTitleComparator c) {
            c.setLanguage(language);
        }
        super.sort();
    }

    public void sortAll() {
        sort();
        for (SeiteSO seite : this) {
            seite.getSeiten().sortAll();
        }
    }

    public List<CommentWithSeite> getAllNotes() {
        List<CommentWithSeite> ret = new ArrayList<>();
        for (SeiteSO seite : this) {
            findAllNotes(seite, new SeiteCommentService(seite).getComments(), ret);
            ret.addAll(seite.getSeiten().getAllNotes());
        }
        return ret;
    }
    
    private void findAllNotes(SeiteSO seite, List<Comment> notes, List<CommentWithSeite> result) {
        for (Comment note : notes) {
            result.add(new CommentWithSeite(note, seite));
            findAllNotes(seite, note.getComments(), result); // recursive
        }
    }

    public SeiteSO getLastChange() {
        SeiteSO maxSeite = null;
        PageChange maxChange = null;
        for (SeiteSO seite : this) {
            PageChange c = seite.getLastChange();
            if (later(c, maxChange)) {
                maxSeite = seite;
                maxChange = c;
            }
            SeiteSO sub = seite.getSeiten().getLastChange(); // recursive
            c = sub == null ? null : sub.getLastChange();
            if (later(c, maxChange)) {
                maxSeite = sub;
                maxChange = c;
            }
        }
        return maxSeite;
    }

    private boolean later(PageChange test, PageChange max) {
        return test != null && (max == null || test.getDate().compareTo(max.getDate()) > 0);
    }

    public TreeSet<SeiteSO> findLink(String href, List<String> langs, ExtractLinksContext xlctx) {
        TreeSet<SeiteSO> ret = new TreeSet<>();
        for (SeiteSO seite : this) {
            for (String lang : langs) {
                if (xlctx.extractLinks(seite, lang).stream().anyMatch(link -> link.getHref().equals(href))) {
                    ret.add(seite);
                    break;
                }
            }
            ret.addAll(seite.getSeiten().findLink(href, langs, xlctx)); // recursive
        }
        return ret;
    }
    
    /**
     * @param langs -
     * @param book -
     * @param results -
     * @see SeiteSO#linksTo(List)
     */
    public void linksTo(List<String> langs, BookSO book, ExtractLinksContext xlctx, TreeSet<SeiteSO> results) {
        forEach(seite -> {
            results.addAll(book.getSeiten().findLink(seite.getId(), langs, xlctx));
            seite.getSeiten().linksTo(langs, book, xlctx, results); // recursive
        });
    }

    public List<TreeItem> getTreeItems(String lang, String currentPageId, TreeItem parent) {
        List<TreeItem> ret = new ArrayList<>();
        for (SeiteSO seite : this) {
            int hc = seite.hasContent(lang);
            if (hc > 0) {
                BookSO book = seite.getBook();
                TreeItem treeItem = new TreeItem(seite.getId(),
                        seite.getSeite().getTitle().getString(lang),
                        hc,
                        book.getBook().getFolder(),
                        book.getWorkspace().getBranch(),
                        seite.getId().equals(currentPageId),
                        parent);
                if (treeItem.isCurrent()) {
                    treeItem.setExpanded(true);
                    TreeItem p = treeItem.getParent();
                    while (p != null) {
                        p.setExpanded(true);
                        p = p.getParent();
                    }
                }
                ret.add(treeItem);
                treeItem.setSubitems(seite.getSeiten().getTreeItems(lang, currentPageId, treeItem));
            }
        }
        return ret;
    }
}
