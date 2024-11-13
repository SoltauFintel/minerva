package minerva.user;

import java.util.List;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Page;
import minerva.MinervaWebapp;
import minerva.base.CustomErrorPage;
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
    protected boolean isAdmin = false;

    @Override
    public void init(Context ctx) {
        super.init(ctx);
        user = StatesSO.get(ctx).getUser();
        langs = MinervaWebapp.factory().getLanguages();
        model.put("N", "en".equals(user.getGuiLanguage()) ? NLS.dataMap_en : NLS.dataMap_de); // RB texts
        isAdmin = UserSO.isAdmin(ctx);
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
        return NLS.get(user.getGuiLanguage(), key);
    }

    protected final void setJQueryObenPageMode() {
        put("jstree", true);
    }
    
    protected final void setCKEditorPageMode() { // EditSeitePage, EditCommentPage
        put("ckeditor", true);
        put("jstree", true);
    }
    
    protected final void setMathPageMode() {
        put("math", true);
    }
    
    protected final void setMultiselectPageMode() {
        put("multiselect", true);
    }
    
    protected final void setMathMultiselectPageMode() {
        setMathPageMode();
        setMultiselectPageMode();
        put("jstree", true);
    }
    
    protected final void showErrorPage(String msg, String continueLink) {
    	CustomErrorPage.showErrorPage(msg, continueLink, ctx);
    }
}
