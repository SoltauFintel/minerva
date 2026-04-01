package minerva.book.multiselect;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.base.SimpleIdAndLabel;
import minerva.base.UserMessage;
import minerva.book.BPage;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

public class MultiSelectPage extends BPage {
    public static Supplier<List<MultiSelectChange>> changesSupplier = () -> List.of(new AddTagMSC(), new RemoveTagMSC(),
            new ClearTagsMSC());
    /** set ahc */
    public static Supplier<Object> additionalHtmlContext = () -> null;
    private Object ahc; // context object for additionalHtml calls
    public static MultiSelectPageDeliverHtmlContent additionalHtml = (_ahc, seite) -> "";
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
        String changeLabelKey = ctx.formParam("action");

        if (selectedPages.isEmpty()) {
            throw new UserMessage("multiSelectError1", book.getUser());
        }
        Logger.info(book.getUser().getLogin() + " | Multi select | change: " + changeLabelKey + " | tag: " + tag);
        
        getChange(changeLabelKey).execute(book, selectedPages, tag);
        
        ctx.redirect("select");
    }
    
    private MultiSelectChange getChange(String changeLabelKey) {
        var changes = changesSupplier.get();
        for (MultiSelectChange change : changes) {
            if (change.getLabelKey().equals(changeLabelKey)) {
                return change;
            }
        }
        throw new RuntimeException("MultiSelectChange not found");
    }

    private void display() {
        header(book.getBook().getTitle().getString(user.getPageLanguage()) + " (" + n("Mehrfachauswahl") + ")");
        ahc = additionalHtmlContext.get();
        put("gliederung", gliederung(book.getSeiten()));
        ahc = null;
        put("hasLeftArea", true);
        List<IdAndLabel> actions = getActions();
        combobox_idAndLabel("actions", actions, actions.get(0).getId(), false);
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
                        + esc(tag) + "\" tabindex=\"-1\">" + esc(tag) + "</a></span>";
            }
            ret += additionalHtml.getHTML(ahc, seite);
            ret += gliederung(seite.getSeiten()); // rekursiv
            ret += "</li>";
        }
        return ret + "</ul>";
    }

    private List<IdAndLabel> getActions() {
        return changesSupplier.get().stream()
                .map(change -> (IdAndLabel) new SimpleIdAndLabel(change.getLabelKey(), n(change.getLabelKey())))
                .toList();
    }
    
    public interface MultiSelectPageDeliverHtmlContent {
        
        String getHTML(Object context, SeiteSO seite); 
    }
}
