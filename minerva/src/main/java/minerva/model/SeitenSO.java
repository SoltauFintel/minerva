package minerva.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.MList;
import minerva.seite.Breadcrumb;
import minerva.seite.IBreadcrumbLinkBuilder;
import minerva.seite.Note;
import minerva.seite.PageChange;
import minerva.seite.Seite;
import minerva.seite.link.LinkService;
import minerva.seite.note.NoteWithSeite;

public class SeitenSO extends MList<SeiteSO> {

    public SeitenSO(ISeite parent) {
        super(getComparator(parent));
    }

    private static Comparator<SeiteSO> getComparator(ISeite parent) {
        if (parent.isSorted()) {
            return new SeiteTitleComparator();
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
            if (seite.getSeiten().getBreadcrumbs(seiteId, builder, breadcrumbs)) {
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

    public List<NoteWithSeite> getAllNotes() {
        List<NoteWithSeite> ret = new ArrayList<>();
        for (SeiteSO seite : this) {
            findAllNotes(seite, seite.getSeite().getNotes(), ret);
            ret.addAll(seite.getSeiten().getAllNotes());
        }
        return ret;
    }
    
    private void findAllNotes(SeiteSO seite, List<Note> notes, List<NoteWithSeite> result) {
        for (Note note : notes) {
            result.add(new NoteWithSeite(note, seite));
            findAllNotes(seite, note.getNotes(), result); // recursive
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

    public List<SeiteSO> findLink(String href, List<String> langs) {
        List<SeiteSO> ret = new ArrayList<>();
        for (SeiteSO seite : this) {
            for (String lang : langs) {
                if (LinkService.extractLinks(seite.getContent().getString(lang), false).stream()
                        .anyMatch(link -> link.getHref().equals(href))) {
                    ret.add(seite);
                    break;
                }
            }
            ret.addAll(seite.getSeiten().findLink(href, langs)); // recursive
        }
        return ret;
    }
}
