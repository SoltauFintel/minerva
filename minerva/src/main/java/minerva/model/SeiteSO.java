package minerva.model;

import static minerva.access.DirAccess.IMAGE;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import minerva.MinervaWebapp;
import minerva.base.NlsString;
import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.seite.Note;
import minerva.seite.Seite;
import minerva.seite.tag.TagNList;

public class SeiteSO implements HasSeiten, Seitensortierung {
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
    public SeiteSO(BookSO book, Seite seite, Seitensortierung parent) {
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

    @Override
    public String getId() {
        return seite.getId();
    }

    @Override
    public boolean isSorted() {
        return seite.isSorted();
    }

    @Override
    public String getUserLanguage() {
        return book.getUser().getLanguage();
    }
    
    @Override
    public String getTitle() {
        return seite.getTitle().getString(getUserLanguage());
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

    public NlsString getContent() {
        if (content == null) {
            content = new NlsString();
            List<String> langs = MinervaWebapp.factory().getLanguages();
            Map<String, String> files = loadFiles(false, langs);
            setContent(files, langs);
        }
        return content;
    }

    public void save(NlsString newTitle, NlsString newContent, int version, List<String> langs) {
        if (getSeite().getVersion() != version) {
            throw new UserMessage("error.simultaneousEditing", book.getWorkspace());
        }
        for (String lang : langs) {
            if (StringService.isNullOrEmpty(newTitle.getString(lang))) {
                throw new UserMessage("error.enterTitle", book.getWorkspace());
            }
        }
        
        seite.setVersion(seite.getVersion() + 1);
        if (content == null) {
            content = new NlsString();
        }
        for (String lang : langs) {
            seite.getTitle().setString(lang, newTitle.getString(lang));
            content.setString(lang, newContent.getString(lang));
        }
        
        Map<String, String> files = new HashMap<>();
        files.put(filenameMeta(), StringService.prettyJSON(seite));
        langs.forEach(lang -> files.put(filenameHTML(lang), pettyHTML(lang)));
        images.forEach(filename -> files.put(filenameIMAGE(filename), IMAGE));
        
        book.dao().saveFiles(files, newTitle.getString(langs.get(0)), book.getWorkspace());
        
        images.clear();

        if (hasNoParent()) {
            // Wenn book.sorted=true ist und ein Seitentitel geändert worden ist, muss neu sortiert werden.
            book.getSeiten().sort();
        }
    }
    
    public void saveTo(Map<String,String> files) {
        seite.setVersion(seite.getVersion() + 1);
        String json = StringService.prettyJSON(seite);
        files.put(filenameMeta(), json);
    }

    public void saveTo_withHTML(Map<String,String> files, List<String> langs) {
        saveTo(files);
        langs.forEach(lang -> files.put(filenameHTML(lang), pettyHTML(lang)));
    }
    
    private String filenameMeta() {
        return book.getFolder() + "/" + getId() + META_SUFFIX;
    }
    
    private String filenameHTML(String lang) {
        return book.getFolder() + "/" + lang + "/" + getId() + ".html";
    }

    private String filenameIMAGE(String filename) {
        return book.getFolder() + "/" + filename;
    }

    private String pettyHTML(String lang) {
        return StringService.prettyHTML(content.getString(lang));
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
            filenames.add(filenameHTML(lang));
        }
        return book.dao().loadFiles(filenames);
    }

    private void setContent(Map<String, String> files, List<String> langs) {
        for (String lang : langs) {
            content.setString(lang, files.get(filenameHTML(lang)));
        }
    }

    public List<String> getImages() {
        return images;
    }

    /**
     * Nach dem Aufruf ist die komplette BookSO/SeiteSO-Struktur neu aufzubauen.
     */
    public void remove() {
        Set<String> filenamesToDelete = new HashSet<>();
        List<String> langs = MinervaWebapp.factory().getLanguages();
        remove(filenamesToDelete, langs);
        List<String> cantBeDeleted = new ArrayList<>();
        
        book.dao().deleteFiles(filenamesToDelete, "delete page " + getId(), book.getWorkspace(), cantBeDeleted);
        book.getWorkspace().pull(); // Datenstruktur neu aufbauen (auch wenn cantBeDeleted nicht leer ist)
        
        if (cantBeDeleted.isEmpty()) {
            Logger.info("Delete page " + getId() + " complete.");
        } else {
            Logger.error("Tried to delete page " + getId() + ": These files could not be deleted:"
                    + cantBeDeleted.stream().map(i -> "\n  - " + i).collect(Collectors.joining())
                    + "\n  Please delete these files manually.");
            throw new RuntimeException("Es konnten nicht alle Dateien gelöscht werden!"
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
        langs.forEach(lang -> filenamesToDelete.add(filenameHTML(lang)));
    }
    
    // similar method in BookSO
    public void saveSubpagesAfterReordering(SeitenSO reorderdSeiten) {
        this.seiten = reorderdSeiten;
        Map<String, String> files = new HashMap<>();
        if (getSeite().isSorted()) {
            getSeite().setSorted(false);
            files.put(filenameMeta(), StringService.prettyJSON(getSeite()));
        }
        reorderdSeiten.setPositionsAndSaveTo(files);
        book.dao().saveFiles(files, "reordering subpages of: " + getTitle(), book.getWorkspace());
    }

    public void activateSorted() {
        seite.setSorted(true);
        saveMeta("sort subpages alphabetically: $t");
        book.getWorkspace().pull(); // ja, ist etwas brutal...
    }

    public void saveMeta(String commitMessage) {
        Map<String, String> files = new HashMap<>();
        files.put(filenameMeta(), StringService.prettyJSON(getSeite()));
        book.dao().saveFiles(files, commitMessage.replace("$t", getTitle()), book.getWorkspace());
    }

    public void addTag(String tag) {
        boolean dirty = false;
        if (tag.contains(",")) {
            for (String aTag : tag.split(",")) {
                if (addTag2(aTag)) {
                    dirty = true;
                }
            }
        } else {
            dirty = addTag2(tag);
        }
        if (dirty) {
            saveMeta("added tag " + tag + " to page: $t");
        }
    }

    private boolean addTag2(String pTag) {
        String tag = cleanTag(pTag);
        if (!seite.getTags().contains(tag)) {
            seite.getTags().add(tag);
            return true;
        }
        return false;
    }
    
    public static String cleanTag(String pTag) {
        String tag = pTag.toLowerCase().trim();
        while (tag.contains("  ")) {
            tag = tag.replace("  ", " ");
        }
        return tag.replace(" ", "-");
    }

    public void removeTag(String tag) {
        if ("$all".equals(tag)) {
            seite.getTags().clear();
            saveMeta("removed all tags from page: $t");
        } else {
            seite.getTags().remove(tag);
            saveMeta("removed tag " + tag + " from page: $t");
        }
    }

    public void saveHTML(String commitMessage, List<String> langs) {
        Map<String, String> files = new HashMap<>();
        langs.forEach(lang -> files.put(filenameHTML(lang), pettyHTML(lang)));
        book.dao().saveFiles(files, commitMessage.replace("$t", getTitle()), book.getWorkspace());
    }

    public List<SeiteSO> findTag(String tag) {
        List<SeiteSO> ret = new ArrayList<>();
        if (seite.getTags().contains(tag)) {
            ret.add(this);
        }
        for (SeiteSO sub : getSeiten()) {
            ret.addAll(sub.findTag(tag));
        }
        return ret;
    }

    public void addAllTags(TagNList tags) {
        seite.getTags().forEach(tag -> tags.add(tag));
        seiten.forEach(seite -> seite.addAllTags(tags));
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

    public void addNote(String text, Note parent) {
        Note note = new Note();
        // TODO zuletzt vergebene number in der Seite merken
        note.setNumber(1 + fetchMax(seite.getNotes()));
        note.setUser(book.getUser().getUser().getLogin());
        note.setCreated(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        note.setChanged("");
        note.setText(text);
        if (parent == null) {
            seite.getNotes().add(note);
        } else {
            parent.getNotes().add(note);
        }
        saveMeta(getTitle() + ": add note #" + note.getNumber());
    }

    private int fetchMax(List<Note> notes) {
        int max = 0;
        for (Note note : notes) {
            if (note.getNumber() > max) {
                max = note.getNumber();
            }
            int m = fetchMax(note.getNotes());
            if (m > max) {
                max = m;
            }
        }
        return max;
    }

    public Note noteByNumber(int number) {
        Note note = _noteByNumber(seite.getNotes(), number);
        if (note == null) {
            throw new RuntimeException("Note not found");
        }
        return note;
    }

    private Note _noteByNumber(List<Note> notes, int number) {
        for (Note note : notes) {
            if (note.getNumber() == number) {
                return note;
            }
            Note note2 = _noteByNumber(note.getNotes(), number);
            if (note2 != null) {
                return note2;
            }
        }
        return null;
    }
    
    public String getLogin() {
        return book.getUser().getUser().getLogin();
    }

    public void deleteNote(int number) {
        if (_deleteNote(seite.getNotes(), number)) {
            saveMeta(getTitle() + ": delete note #" + number);
        }
    }
    
    private boolean _deleteNote(List<Note> notes, int number) {
        for (Note note : notes) {
            if (note.getNumber() == number) {
                notes.remove(note);
                return true;
            }
            boolean ret = _deleteNote(note.getNotes(), number);
            if (ret) {
                return ret;
            }
        }
        return false;
    }
    
    public int getNotesSize() {
        return _getNotesSize(seite.getNotes());
    }

    private int _getNotesSize(List<Note> notes) {
        int ret = notes.size();
        for (Note note : notes) {
            ret += _getNotesSize(note.getNotes());
        }
        return ret;
    }
}
