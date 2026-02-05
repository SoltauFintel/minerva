package minerva.book;

import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.base.SimpleIdAndLabel;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class MultiSelectPage extends BPage {
    private static final String ADD = "1";
    private static final String CLEAR = "00";
    private Set<String> selectedPages;
    
    @Override
    protected void execute() {
        selectedPages = book.loadMultiSelect();

        if (isPOST()) {
            executeAction();
        } else {
            display();
        }
    }
    
    private void executeAction() {
        String tag = ctx.formParam("tag");
        String action = ctx.formParam("action");
        
        Logger.info(book.getUser().getLogin() + " | Multi select | action: " + action + " | tag: " + tag);
        book.multiSelectAction(selectedPages, tag, CLEAR.equals(action), ADD.equals(action));
        
        ctx.redirect("select");
    }

    private void display() {
        header(book.getBook().getTitle().getString(user.getPageLanguage()) + " (" + n("Mehrfachauswahl") + ")");
        put("gliederung", gliederung(book.getSeiten()));
        put("hasLeftArea", true);
        combobox_idAndLabel("actions", getActions(), ADD, false);
    }
    
    private String gliederung(SeitenSO seiten) {
        if (seiten.isEmpty()) {
            return "";
        }
        String ret = "<ul class=\"no-bullets\">";
        String lang = user.getPageLanguage();
        String bb = branch + "/" + bookFolder + "/";
        for (SeiteSO seite : seiten) {
            String ch = selectedPages.contains(seite.getId()) ? " checked" : "";
            ret += "<li><input type=\"checkbox\" name=\"v\" hx-post=\"/b/" + bb + "select-page?id=" + seite.getId()
                    + "\" hx-trigger=\"change\"" + ch + "> <a href=\"/s/" + bb + seite.getId()
                    + "\" target=\"_blank\" tabindex=\"-1\" class=\"mr1\">" + esc(seite.getSeite().getTitle().getString(lang))
                    + "</a>";
            for (String tag : seite.getSeite().getTags()) {
                ret += " <span class=\"label label-tag\"><i class=\"fa fa-tag\"></i> <a href=\"/w/" + branch + "/tag/"
                        + esc(tag) + "\">" + esc(tag) + "</a></span>";
            }
            ret += gliederung(seite.getSeiten()); // rekursiv
            ret += "</li>";
        }
        return ret + "</ul>";
    }

    private List<IdAndLabel> getActions() {
        return List.of(
                new SimpleIdAndLabel(ADD, n("tag hinzufügen")),
                new SimpleIdAndLabel("0", n("tag entfernen")),
                new SimpleIdAndLabel(CLEAR, n("alle tags entfernen")));
    }
}
