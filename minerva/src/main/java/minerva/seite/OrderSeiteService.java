package minerva.seite;

import java.util.Arrays;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.model.ISeite;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.UserSO;
import minerva.user.UPage;

/**
 * Gemeinsamer Code von OrderSeitePage und OrderTopLevelSeitePage
 * 
 * <p>Es gibt hier 3 Events: startEvent, movedEvent und endEvent. Beim startEvent wird eine Arbeitskopie gemacht,
 * damit beim Abbruch die Original-Seiten nicht verändert werden. Beim movedEvent informiert die GUI über eine
 * einzelne Drag & Drop Aktion = Seitenumordnung. Da gerade bei der Git-Persistenz das Speichern dauert, wird
 * erst im endEvent gespeichert.</p>
 */
public abstract class OrderSeiteService {
    private final Context ctx;
    private final boolean post;
    private final DataMap model;
    private final String branch;
    private final String bookFolder;
    private final String id;
    /** gleichzeitig auch Basislink */
    private final String goBackLink; // viewlink
    private final UserSO user;
    // title, subpages
    private final ISeite hasSeiten;
    
    public OrderSeiteService(Context ctx, boolean post, DataMap model, String branch, String bookFolder, String id,
            String goBackLink, UserSO user, ISeite hasSeiten) {
        this.ctx = ctx;
        this.post = post;
        this.model = model;
        this.branch = branch;
        this.bookFolder = bookFolder;
        this.id = id;
        this.goBackLink = goBackLink;
        this.user = user;
        this.hasSeiten = hasSeiten;
    }

    public void execute() {
        SeitenSO seitenWC; // working copy
        if (post) {
            seitenWC = movedEvent();
        } else if (isEndEvent()) {
            seitenWC = endEvent(branch, bookFolder, id);
            fill(branch, bookFolder, id, hasSeiten);
        } else { // init
            seitenWC = startEvent(hasSeiten.getSeiten());
            fill(branch, bookFolder, id, hasSeiten);
        }

        ViewSeitePage.fillSubpages(seitenWC, user.getGuiLanguage(), model.list("subpages"), branch, bookFolder);
    }

    private SeitenSO startEvent(SeitenSO seiten) {
        SeitenSO seitenWC = createSeitenSO(id); // create working copy
        int position = 1;
        for (SeiteSO sub : seiten) {
            SeiteSO copy = new SeiteSO(sub);
            copy.getSeite().setPosition(position++);
            seitenWC.add(copy);
        }
        user.setOrderPagesModel(seitenWC);
        return seitenWC;
    }

    private SeitenSO movedEvent() {
        List<String> neueReihenfolge = Arrays.asList(ctx.req.queryParamsValues("item"));
        SeitenSO seitenWC = fetchSeiten();
        seitenWC.order(neueReihenfolge, (seite, id) -> seite.getId().equals(id));
        int position = 1;
        for (SeiteSO s : seitenWC) {
            s.getSeite().setPosition(position++);
        }
        return seitenWC;
    }

    // whole action completed, save it
    private SeitenSO endEvent(String branch, String bookFolder, String id) {
        SeitenSO seitenWC = fetchSeiten();
        saveSubpagesAfterReordering(seitenWC);
        user.setOrderPagesModel(null);
        Logger.info("Subpages have been saved after reordering.");
        
        ctx.redirect(goBackLink);
        return seitenWC;
    }
    
    protected abstract void saveSubpagesAfterReordering(SeitenSO seitenWC);

    private boolean isEndEvent() {
        return "end".equals(ctx.queryParam("m"));
    }

    private SeitenSO fetchSeiten() {
        SeitenSO seitenWC = user.getOrderPagesModel();
        if (seitenWC == null) {
            throw new RuntimeException("Page reordering is corrupt. Please reload page.");
        }
        return seitenWC;
    }

    private void fill(String branch, String bookFolder, String id, ISeite seite) {
        String title = n("reorderSubpages");
        model.put("header", title);
        model.put("title", title + UPage.TITLE_POSTFIX);
        model.put("pageTitle", Escaper.esc(seite.getTitle()));
        model.put("orderlink", goBackLink + "/order");
        model.put("fertiglink", goBackLink + "/order?m=end");
    }

    public String render() {
        if (post) {
            // Beim Umordnen wird aufgrund der htmx/SortableJS-Technik nur das innere HTML an die GUI gesendet.
            return Page.templates.render("OrderSeitePageSubpages", model);
        } else if (isEndEvent()) {
            return "";
        } else {
            return Page.templates.render("OrderSeitePage", model);
        }
    }
    
    private String n(String key) {
        return NLS.get(user.getGuiLanguage(), key);
    }

    private SeitenSO createSeitenSO(String seiteId) {
        return new SeitenSO(new ISeite() {
            @Override
            public boolean isSorted() {
                return false;
            }
            
            @Override
            public String getId() {
                return seiteId;
            }

            @Override
            public String getTitle() {
                throw new UnsupportedOperationException();
            }

            @Override
            public SeitenSO getSeiten() {
                throw new UnsupportedOperationException();
            }
        });
    }
}
