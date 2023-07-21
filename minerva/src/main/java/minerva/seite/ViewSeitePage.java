package minerva.seite;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.Uptodatecheck;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.UserSettingsSO;

public class ViewSeitePage extends SPage implements Uptodatecheck {
    private boolean returnEmpty = false;
    
    @Override
    protected SeiteSO getSeite() {
        return book.getSeiten()._byId(id);
    }
    
    @Override
    protected void execute() {
        if (id == null || id.isBlank() || !esc(id).equals(id)) {
            throw new RuntimeException("Illegal page ID");
        }
        if (seite == null) {
            Logger.error("Page not found: " + id);
            returnEmpty = true;
            ctx.redirect("/message?m=1");
            return;
        }
        execute2(branch, bookFolder, id, seite);
    }
    
    protected void execute2(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        seiteSO.forceReloadIfCheap();
        Seite seite = seiteSO.getSeite();
        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            map.put("LANG", lang.toUpperCase());
            map.put("lang", lang);
            map.put("titel", esc(seite.getTitle().getString(lang)));
            map.put("content", seiteSO.getContent().getString(lang));
            map.put("active", lang.equals(user.getPageLanguage()));
            fillBreadcrumbs(lang, map.list("breadcrumbs"));
            map.putInt("subpagesSize", fillSubpages(seiteSO.getSeiten(), lang, map.list("subpages"),
                    branch, bookFolder, false));
        }
        
        fillTags(seite);
        putInt("tagsSize", seiteSO.getSeite().getTags().size());
        
        put("book", bookFolder);
        put("id", id);
        put("parentId", esc(seite.getParentId()));
        putInt("position", seite.getPosition());
        putInt("version", seite.getVersion());
        put("bookTitle", esc(seiteSO.getBook().getBook().getTitle().getString(user.getPageLanguage()))); // bin usicher
        put("hasSubPages", !seiteSO.getSeiten().isEmpty());
        put("Sortierung", n(seite.isSorted() ? "alfaSorted" : "manuSorted"));
        put("isSorted", seite.isSorted());
        int n = seiteSO.notes().getNotesSize();
        putInt("notesSize", n);
        put("hasNotes", n > 0);
        PageChange change = seiteSO.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            fillLastChange(change, seiteSO.getTitle(), n("lastChangeInfo"), model);
        }
        putInt("helpKeysSize", seite.getHelpKeys().size());
        String oneHelpKey = getOneHelpKey(seite.getHelpKeys());
        put("oneHelpKey", esc(oneHelpKey));
        put("hasOneHelpKey", !oneHelpKey.isEmpty());
        UserSettingsSO us = user.getUserSettings();
        put("isFavorite", us.getFavorites().contains(id));
        put("pageWatched", us.getWatchlist().contains(id));
        put("subpagesWatched", us.getWatchlist().contains(id + "+"));
        put("ctrlS", n("ctrlS"));
        header(modifyHeader(seiteSO.getTitle()));

        fillLinks(branch, bookFolder, id, seiteSO, seite);
        Logger.info(user.getUser().getLogin() + " | " + seiteSO.getBook().getWorkspace().getBranch() + " | "
                + seiteSO.getTitle() + " | " + user.getPageLanguage());
    }
    
    public static void fillLastChange(PageChange change, String pageTitle, String infotext, DataMap model) {
        String lastChangeInfo = infotext
            .replace("$d", change.getDate())
            .replace("$u", change.getUser())
            .replace("$c", change.getComment().isEmpty() ? "" : (": " + change.getComment()))
            .replace("$p", pageTitle);
        model.put("lastChangeInfo", Escaper.esc(lastChangeInfo));
        model.put("lastChange", Escaper.esc(change.getComment()));
        model.put("lastChangeDate", Escaper.esc(change.getDate()));
        model.put("lastChangeUser", Escaper.esc(change.getUser()));
    }
    
    static int fillSubpages(SeitenSO seiten, String lang, DataList subpages, String branch, String bookFolder,
            boolean showAllPages) {
        int n = 0;
        seiten.sort(lang);
        for (SeiteSO sub : seiten) {
            if (showAllPages || sub.hasContent(lang) > 0) {
                DataMap map = subpages.add();
                map.put("id", Escaper.esc(sub.getId()));
                map.put("titel", Escaper.esc(sub.getSeite().getTitle().getString(lang)));
                map.put("viewlink", "/s/" + branch + "/" + bookFolder + "/" + Escaper.esc(sub.getId()));
                map.putInt("position", sub.getSeite().getPosition());
                n++;
            }
        }
        return n;
    }

    private void fillTags(Seite seite) {
        DataList tags = list("tags");
        seite.getTags().stream().sorted().forEach(tag -> {
            DataMap map = tags.add();
            map.put("tag", esc(tag));
            map.put("link", "/w/" + branch + "/tag/" + tag);
        });
        put("hasTags", !seite.getTags().isEmpty());
    }
    
    protected String modifyHeader(String header) {
        return header;
    }

    private void fillLinks(String branch, String bookFolder, String id, SeiteSO seiteSO, Seite seite) {
        String onlyBookFolder = "/s/" + branch + "/" + bookFolder + "/";

        // Navigation
        String booklink = "/b/" + branch + "/" + bookFolder;
        put("booklink", booklink);
        put("parentlink", seiteSO.hasParent() ? (onlyBookFolder + seite.getParentId()) : booklink);
        NavigateService nav = new NavigateService(true, user.getPageLanguage(), null);
        navlink("prevlink", nav.previousPage(seiteSO), id, onlyBookFolder);
        navlink("nextlink", nav.nextPage(seiteSO), id, onlyBookFolder);
        put("tabcode", getTabCode(nav, seiteSO, id, onlyBookFolder));
        
        // Standard
        String withSeiteId = onlyBookFolder + id;
        put("viewlink", withSeiteId);
        put("createlink", withSeiteId + "/add");
        put("pulllink", withSeiteId + "/pull");
        put("positionlink", withSeiteId + "/order");
        put("sortlink", withSeiteId + "/sort");
        put("edittagslink", withSeiteId + "/tags");
        put("movelink", withSeiteId + "/move-select-target");
        put("deletelink", withSeiteId + "/delete");
        
        // Edit
        put("editlink", "/s-edit/" + branch + "/" + bookFolder + "/" + id);
        put("postcontentslink", withSeiteId + "/post-contents");
        put("imageuploadlink", "/s-image-upload/" + branch + "/" + bookFolder + "/" + id);
        
        put("hasPositionlink", seiteSO.getSeiten().size() > 1);
    }
    
    private void navlink(String name, SeiteSO nav, String seiteId, String onlyBookFolder) {
        String nav_id = nav.getId();
        String has = "has" + name.substring(0, 1).toUpperCase() + name.substring(1);
        put(has, !nav_id.equals(seiteId));
        put(name, onlyBookFolder + nav_id);
    }
    
    private String getTabCode(NavigateService nav0, SeiteSO seiteSO, String seiteId, String onlyBookFolder) {
        String ret = "", ret2 = "";
        for (String lang : langs) {
            NavigateService nav = new NavigateService(true, lang, null);
            SeiteSO prevSeite = nav.previousPage(seiteSO);
            boolean has;
            String link;
            if (prevSeite.getId().equals(seiteId)) {
                has = false;
                link = "";
            } else {
                has = true;
                link = onlyBookFolder + prevSeite.getId();
            }
            ret += "if (lang == \"" + lang + "\") {\n";
            ret += "  $(\"#prev1\").attr(\"disabled\", " + !has + ")\n";
            ret += "  $(\"#prev1\").attr(\"href\", \"" + link + "\")\n";
            ret += "  $(\"#prev2\").attr(\"disabled\", " + !has + ")\n";
            ret += "  $(\"#prev2\").attr(\"href\", \"" + link + "\")\n";
            ret += "}\n";
            ret2 += "$(\"#subpages_" + lang + "\").attr(\"hidden\", lang != \"" + lang + "\")\n";
        }
        for (String lang : langs) {
            NavigateService nav = new NavigateService(true, lang, null);
            SeiteSO prevSeite = nav.nextPage(seiteSO);
            boolean has;
            String link;
            if (prevSeite.getId().equals(seiteId)) {
                has = false;
                link = "";
            } else {
                has = true;
                link = onlyBookFolder + prevSeite.getId();
            }
            ret += "if (lang == \"" + lang + "\") {\n";
            ret += "  $(\"#next1\").attr(\"disabled\", " + !has + ")\n";
            ret += "  $(\"#next1\").attr(\"href\", \"" + link + "\")\n";
            ret += "  $(\"#next2\").attr(\"disabled\", " + !has + ")\n";
            ret += "  $(\"#next2\").attr(\"href\", \"" + link + "\")\n";
            ret += "}\n";
        }
        return ret + ret2;
    }

    private void fillBreadcrumbs(String lang, DataList list) {
        List<Breadcrumb> breadcrumbs = book.getBreadcrumbs(id, new ViewAreaBreadcrumbLinkBuilder());
        for (int i = breadcrumbs.size() - 1; i >= 0; i--) {
            Breadcrumb b = breadcrumbs.get(i);
            DataMap map = list.add();
            map.put("title", esc(b.getTitle().getString(lang)));
            map.put("link", b.getLink());
            map.put("first", i == breadcrumbs.size() - 1);
            map.put("last", i == 0);
        }
    }
    
    @Override
    protected String render() {
        if (returnEmpty) {
            return "";
        }
        return super.render();
    }
    
    private String getOneHelpKey(List<String> helpKeys) {
        if (helpKeys.size() == 1) {
            String ret = helpKeys.get(0).trim();
            if (!ret.isEmpty() && ret.length() <= 40) {
                return ret;
            }
        }
        return "";
    }
}
