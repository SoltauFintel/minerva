package minerva.seite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import minerva.model.BookSO;
import minerva.model.ISeite;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

/**
 * Es soll eine Seite, für den der Titel gegeben ist, angelegt werden.
 * Und zwar unter einer Seite, deren tag gegeben ist.
 * Falls es letztere Seite nicht gibt, werden die übergeordneten Seiten anhand einer Titel-Hierarchie angelegt.
 * Die davon letzte Seite erhält das gegebene tag.
 * 
 * Mit dem tag kann also der Anwender steuern, wohin die Seite angelegt wird.
 * Der Algorithmus stellt sicher, dass die parent-Seiten nicht ständig neu angelegt werden.
 */
public class CreateSeiteUnderTag {
    private final BookSO book;
    private String lang;
    private Map<String, String> files;
    
    /**
     * @param book in welchem Buch die Seite angelegt werden soll
     * @param lang Sprache des Titels für die neu anzulegende Seite
     */
    public CreateSeiteUnderTag(BookSO book, String lang) {
        this.book = book;
        this.lang = lang;
    }

    public SeiteSO createSeite(String title, String parentTag, String parentTitlePath) {
        SeiteSO seite;
        SeiteSO parent = byTag(parentTag);
        files = new HashMap<>();
        if (parent == null) {
            String[] ptp = parentTitlePath.split("/");
            for (int i = 0; i < ptp.length; i++) {
                parent = findOrCreateSeite(ptp, i, i == ptp.length - 1 ? parentTag : "", parent);
            }
        } else {
            seite = parent.getSeiten()._byTitle(title, lang);
            if (seite != null) {
                return seite;
            }
        }
        seite = createSeite(parent, title);
        book.dao().saveFiles(files, new CommitMessage(title), book.getWorkspace());
        files = null;
        return seite;
    }

    private SeiteSO byTag(String tag) {
        List<SeiteSO> seiten = book.findTag(tag);
        if (seiten.size() == 0) {
            return null;
        } else if (seiten.size() > 1) {
            throw new RuntimeException("Can't create page because there is more than one page with tag '" + tag + "'! Please fix that!");
        }
        return seiten.get(0);
    }

    private SeiteSO findOrCreateSeite(String[] ptp, int i, String tag, SeiteSO parent) {
        String title = ptp[i];
        SeitenSO seiten;
        ISeite iseite;
        if (i == 0) {
            seiten = book.getSeiten();
            iseite = book.getISeite();
        } else {
            seiten = parent.getSeiten();
            iseite = parent;
        }
        SeiteSO seite = seiten._byTitle(title, lang);
        if (seite == null) {
            String id = seiten.createSeite(iseite, book, book.dao());
            seite = book._seiteById(id);
            seite.getSeite().getTitle().setString("de", title);
            seite.getSeite().getTitle().setString("en", title);
            if (!tag.isEmpty()) {
                seite.getSeite().getTags().add(tag);
            }
            seite.saveMetaTo(files);
            Logger.info(book.getUser().getLogin() + " | created interface parent page: " + title);
        }
        return seite;
    }

    private SeiteSO createSeite(SeiteSO parent, String title) {
        String id = parent.getSeiten().createSeite(parent, book, book.dao());
        SeiteSO seite = book._seiteById(id);
        
        seite.getSeite().getTitle().setString("de", title);
        seite.getSeite().getTitle().setString("en", title);
        seite.saveMetaTo(files);
        
		seite.getContent().setString(lang, body("TODO"));
		List<String> langs = new ArrayList<>();
		langs.add(lang);
		if ("de".equals(lang)) {
			seite.getContent().setString("en", body("::"));
			langs.add("en");
		} else {
			seite.getContent().setString("de", body("::"));
			langs.add("de");
		}
        seite.saveHtmlTo(files, langs);
        
        Logger.info(book.getUser().getLogin() + " | created interface page: " + title);
        return seite;
    }
    
    private String body(String text) {
    	return "<html><body><p>" + text + "</p></body></html>";
    }
}
