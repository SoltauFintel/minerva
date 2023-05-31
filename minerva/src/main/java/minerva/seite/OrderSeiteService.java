package minerva.seite;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.action.Page;
import minerva.base.NLS;
import minerva.model.HasSeiten;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.Seitensortierung;
import minerva.model.UserSO;
import minerva.user.UPage;

/**
 * Gemeinsamer Code von OrderSeitePage und OrderTopLevelSeitePage
 */
public abstract class OrderSeiteService {
    private static final Map<String, SeitenSO> map = new HashMap<>(); // needs synchronized access
    private static final String handle = "map";
    private final Context ctx;
    private final boolean post;
    private final DataMap model;
    private final String branch;
    private final String bookFolder;
    private final String id;
    /** gleichzeitig auch Basislink */
    private final String goBackLink; // viewlink
    private final UserSO user;
    private final HasSeiten hasSeiten;
    
    public OrderSeiteService(Context ctx, boolean post, DataMap model, String branch, String bookFolder, String id,
            String goBackLink, UserSO user, HasSeiten hasSeiten) {
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
        String key = ctx.queryParam("key");
        
        SeitenSO seitenWC; // working copy
        if (post) {
            seitenWC = movedEvent(key);
        } else if (isEndEvent()) {
            seitenWC = endEvent(branch, bookFolder, id, key);
            fill(branch, bookFolder, id, key, hasSeiten);
        } else { // init
            key = IdGenerator.genId();
            seitenWC = startEvent(hasSeiten.getSeiten(), key);
            fill(branch, bookFolder, id, key, hasSeiten);
        }

        ViewSeitePage.fillSubpages(seitenWC, user.getLanguage(), model.list("subpages"), branch, bookFolder);
    }

    private SeitenSO startEvent(SeitenSO seiten, String key) {
        SeitenSO seitenWC = createSeitenSO(id); // create working copy
        int position = 1;
        for (SeiteSO sub : seiten) {
            SeiteSO copy = new SeiteSO(sub);
            copy.getSeite().setPosition(position++);
            seitenWC.add(copy);
        }
        synchronized (handle) {
            map.put(key, seitenWC);
        }
        model.put("key", key);
        return seitenWC;
    }

    private SeitenSO movedEvent(String key) {
        SeitenSO seitenWC;
        List<String> neueReihenfolge = Arrays.asList(ctx.req.queryParamsValues("item"));
        synchronized (handle) {
            seitenWC = fetchSeiten(key);
            seitenWC.order(neueReihenfolge, (seite, id) -> seite.getId().equals(id));
            int position = 1;
            for (SeiteSO s : seitenWC) {
                s.getSeite().setPosition(position++);
            }
            map.put(key, seitenWC);
        }
        return seitenWC;
    }

    // whole action completed, save it
    private SeitenSO endEvent(String branch, String bookFolder, String id, String key) {
        SeitenSO seitenWC;
        synchronized (handle) {
            seitenWC = fetchSeiten(key);
            saveSubpagesAfterReordering(seitenWC);
        }
        Logger.info("Subpages have been saved after reordering.");
        
        ctx.redirect(goBackLink);
        return seitenWC;
    }
    
    protected abstract void saveSubpagesAfterReordering(SeitenSO seitenWC);

    private boolean isEndEvent() {
        return "end".equals(ctx.queryParam("m"));
    }

    private SeitenSO fetchSeiten(String key) {
        SeitenSO seitenWC = map.get(key);
        if (seitenWC == null) {
            throw new RuntimeException("Page reordering is corrupt. Please reload page.");
        }
        return seitenWC;
    }

    private void fill(String branch, String bookFolder, String id, String key, HasSeiten hasSeiten) {
        String title = n("reorderSubpages");
        model.put("header", title);
        model.put("title", title + UPage.TITLE_POSTFIX);
        model.put("pageTitle", Escaper.esc(hasSeiten.getTitle()));
        model.put("orderlink", goBackLink + "/order?key=" + key);
        model.put("fertiglink", goBackLink + "/order?key=" + key + "&m=end");
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
        return NLS.get(user.getLanguage(), key);
    }

    private SeitenSO createSeitenSO(String seiteId) {
        return new SeitenSO(new Seitensortierung() {
            @Override
            public boolean isSorted() {
                return false;
            }
            
            @Override
            public String getUserLanguage() {
                return user.getLanguage();
            }
            
            @Override
            public String getId() {
                return seiteId;
            }
        });
    }
}
