package minerva.user;

import java.util.List;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.model.StatesSO;
import minerva.model.UserSO;

/**
 * Base class for user pages
 */
public abstract class UPage extends Page {
    public static final String TITLE_POSTFIX = " - Minerva";
    protected UserSO user;
    protected List<String> langs;

    @Override
    public void init(Context ctx) {
        super.init(ctx);
        user = StatesSO.get(ctx).getUser();
        langs = MinervaWebapp.factory().getLanguages();
        model.put("N", "en".equals(user.getLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
    }

    protected void header(String title) {
        String t = esc(title);
        put("header", t);
        put("title", t + TITLE_POSTFIX);
    }
    
    /**
     * NLS
     * @param key -
     * @return text in user language
     */
    protected String n(String key) {
        return NLS.get(user.getLanguage(), key);
    }
    
    protected ColumnFormularGenerator initColumnFormularGenerator(ColumnFormularGenerator gen) {
        return gen
                .save(n("save"))
                .cancel(n("cancel"))
                .submit(" onclick=\"document.querySelector('#s1').style='';\"",
                        "<i id=\"s1\" class=\"fa fa-delicious fa-spin\" style=\"display: none;\"></i>");
    }
}
