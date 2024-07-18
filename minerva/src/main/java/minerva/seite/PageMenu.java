package minerva.seite;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.NLS;
import minerva.model.SeiteSO;

/**
 * Menu for ViewSeitePage
 */
public class PageMenu {
    private final String guiLanguage;
    
    public PageMenu(String guiLanguage) {
        this.guiLanguage = guiLanguage;
    }

    public DataList menu(DataMap model, SeiteSO seite, String viewlink, boolean isAdmin, boolean isFavorite,
            boolean pageWatched, boolean subpagesWatched, boolean gitlab, boolean isCustomerVersion) {
        DataList i = model.list("menuitems");
        String viewLink = model.get("viewlink").toString();
        menuitem(i, viewLink + "/toggle-favorite",
                "fav fa-star" + (isFavorite ? "" : "-o"),
                n("favorite") + (isFavorite ? " <i class=\"fa fa-check greenbook\"></i>" : ""));
        menuitem(i, viewLink + "/toggle-watch",
                "fa-bell" + (pageWatched ? "" : "-o"),
                n("watchPage") + (pageWatched ? " <i class=\"fa fa-check greenbook\"></i>" : ""));
        menuitem(i, viewLink + "/toggle-watch?m=s",
                "fa-sitemap",
                n("watchSubpages") + (subpagesWatched ? " <i class=\"fa fa-check greenbook\"></i>" : ""));
        menuitem(i, " data-toggle=\"modal\" data-target=\"#tocModal\"", "fa-list-ul", n("TOC"));
        menuitem(i, viewLink + "/attachments", "fa-paperclip", "Attachments");
        menuitem(i, "", "", "-");
        
        if (gitlab) {
            menuitem(i, viewLink + "/history", "fa-clock-o", n("history"));
        }
        if (isAdmin) {
            //  li  style="background-color: #ff9;"
            menuitem(i, " data-toggle=\"modal\" data-target=\"#editorsnoteModal\"",
                    "fa-thumb-tack",
                    n("editorsNote"));
            
        }
        if (isCustomerVersion) {
            menuitem(i, viewLink + "/help-keys",
                    "fa-question-circle",
                    n("helpKeys") + " (" + model.get("helpKeysSize").toString() + ")");
        }
        menuitem(i, viewLink + "/links", "fa-link", n("linkAnalysis"));
        additionalMenuItems(i);
        menuitem(i, "", "", "-");
        
        if (isAdmin) {
            menuitem(i, viewlink + "/html", "fa-code", n("editHTML"));
        }
        menuitem(i, "/w/" + Escaper.esc(seite.getBook().getWorkspace().getBranch()) + "/export?seite="
                + Escaper.urlEncode(seite.getId(), ""), "fa-upload", n("exportPage"));
        menuitem(i, model.get("duplicatelink").toString(), "fa-copy", n("duplicatePage"));
        menuitem(i, model.get("movelink").toString(), "fa-arrow-circle-right", n("movePage"));
        menuitem(i, model.get("deletelink").toString(), "fa-trash", n("deletePage") + "...");
        return i;
    }
    
    protected void additionalMenuItems(DataList i) { //
    }

    protected final void menuitem(DataList menuitems, String link, String icon, String label) {
        DataMap map = menuitems.add();
        map.put("link", link);
        map.put("icon", icon);
        map.put("label", label);
        map.put("line", "-".equals(label));
        map.put("attrs", "");
        map.put("liArgs", "");
        if (link.startsWith(" ")) {
            map.put("link", "#");
            map.put("attrs", link);
        } else if (link.contains("/delete")) {
            map.put("attrs", " style=\"color: #900;\"");
        } else if (link.contains("editorsnote")) {
            map.put("liArgs", " style=\"background-color: #ff9;\"");
        }
    }

    protected final String n(String key) {
        return NLS.get(guiLanguage, key);
    }
}
