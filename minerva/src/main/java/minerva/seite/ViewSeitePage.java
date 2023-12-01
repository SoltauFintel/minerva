package minerva.seite;

import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.user.User;
import minerva.user.UserAccess;

public class ViewSeitePage extends SPage implements Uptodatecheck {
    
    @Override
    protected SeiteSO getSeite() {
        return book._seiteById(id);
    }
    
    @Override
    protected void execute() {
        if (id == null || id.isBlank() || !esc(id).equals(id)) {
            throw new RuntimeException("Illegal page ID");
        }
        if (seite == null) {
            Logger.error("Page not found: " + id);
            render = false;
            ctx.redirect("/message?m=1");
            return;
        }
        execute2(branch, bookFolder, id, seite);
    }
    
    protected void execute2(String branch, String bookFolder, String id, SeiteSO seiteSO) {
        User u = user.getFreshUser();
        seiteSO.forceReloadIfCheap();
        Seite seite = seiteSO.getSeite();
        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            map.put("LANG", lang.toUpperCase());
            map.put("lang", lang);
            map.put("titel", esc(seite.getTitle().getString(lang)));
            TocMacro macro = new TocMacro(seiteSO.getTocMacroPage(), "-", lang, "");
            map.put("content", macro.transform(seiteSO.getContent().getString(lang)));
            map.put("toc", macro.getTOC()); // no esc, after transform()
            map.put("active", lang.equals(u.getPageLanguage()));
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
        put("bookTitle", esc(seiteSO.getBook().getBook().getTitle().getString(u.getPageLanguage()))); // bin usicher
        put("hasSubPages", !seiteSO.getSeiten().isEmpty());
        put("Sortierung", n(seite.isSorted() ? "alfaSorted" : "manuSorted"));
        put("isSorted", seite.isSorted());
        int n = seiteSO.notes().getNotesSize();
        putInt("notesSize", n);
        put("hasNotes", n > 0);
        PageChange change = seiteSO.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            fillLastChange(seiteSO, change, esc(n("lastChangeInfo")), model);
        }
        putInt("helpKeysSize", seite.getHelpKeys().size());
        String oneHelpKey = getOneHelpKey(seite.getHelpKeys());
        put("oneHelpKey", esc(oneHelpKey));
        put("hasOneHelpKey", !oneHelpKey.isEmpty());
        boolean isFavorite = u.getFavorites().contains(id);
		put("isFavorite", isFavorite);
        boolean pageWatched = u.getWatchlist().contains(id);
		put("pageWatched", pageWatched);
        boolean subpagesWatched = u.getWatchlist().contains(id + "+");
		put("subpagesWatched", subpagesWatched);
        put("ctrlS", n("ctrlS"));
        levellist("levellist", seite.getTocHeadingsLevels());
        levellist("levellist2", seite.getTocSubpagesLevels());
        put("editorsNote", esc(seite.getEditorsNote()));
        put("editorsNoteBR", esc(seite.getEditorsNote()).replace("\n", "<br/>"));
        put("hasEditorsNote", !StringService.isNullOrEmpty(seite.getEditorsNote()));
        put("hasLeftArea", true);
        put("leftAreaContent", tree(seiteSO.getBook().getSeiten(), user.getGuiLanguage(), seite.getId()));
        header(modifyHeader(seiteSO.getTitle()));

        fillLinks(branch, bookFolder, id, seiteSO, seite, u.getPageLanguage());
        
        menu(isFavorite, pageWatched, subpagesWatched,
        		MinervaWebapp.factory().getConfig().isGitlab(), MinervaWebapp.factory().isCustomerVersion()); // möglichst spät aufrufen
        
        Logger.info(u.getLogin() + " | " + seiteSO.getBook().getWorkspace().getBranch() + " | "
                + seiteSO.getTitle() + " | " + u.getPageLanguage());
    }
    
    private void levellist(String listId, int levels) {
        DataList list2 = list(listId);
        for (int i = 0; i <= 5; i++) {
            DataMap map = list2.add();
            map.put("text", i == 5 ? "5 (" + n("standard") + ")" : (i == 0 ? "0 (" + n("off") + ")" : "" + i));
            map.put("selected", i == levels);
        }
    }

    public static void fillLastChange(SeiteSO seite, PageChange change, String infotext, DataMap model) {
        String lastChangeInfo = infotext
            .replace("$d", Escaper.esc(change.getDate()))
            .replace("$u", Escaper.esc(UserAccess.login2RealName(change.getUser())))
            .replace("$c", Escaper.esc(change.getComment().isEmpty() ? "" : (": " + change.getComment())))
            .replace("$p", Escaper.esc(seite.getTitle()))
            .replace("$h", Escaper.esc("/s/" + seite.getBook().getWorkspace().getBranch() + "/" + seite.getBook().getBook().getFolder() + "/" + seite.getId()));
        model.put("lastChangeInfo", lastChangeInfo);
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

    private void fillLinks(String branch, String bookFolder, String id, SeiteSO seiteSO, Seite seite, String pageLanguage) {
        String onlyBookFolder = "/s/" + branch + "/" + bookFolder + "/";

        // Navigation
        String booklink = "/b/" + branch + "/" + bookFolder;
        put("booklink", booklink);
        put("parentlink", seiteSO.hasParent() ? (onlyBookFolder + seite.getParentId()) : booklink);
        NavigateService nav = new NavigateService(true, pageLanguage, null);
        navlink("prevlink", nav.previousPage(seiteSO), id, onlyBookFolder, "/b/" + branch + "/" + book.getBook().getFolder());
        navlink("nextlink", nav.nextPage(seiteSO), id, onlyBookFolder, null);
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
    
    private void navlink(String name, SeiteSO nav, String seiteId, String onlyBookFolder, String booklink) {
        String nav_id = nav.getId();
        String hasName = "has" + name.substring(0, 1).toUpperCase() + name.substring(1);
        boolean has = !nav_id.equals(seiteId);
        String link = onlyBookFolder + nav_id;
        if (!has && booklink != null) {
            has = true;
            link = booklink;
        }
        put(hasName, has);
        put(name, link);
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
    
    private String getOneHelpKey(List<String> helpKeys) {
        if (helpKeys.size() == 1) {
            String ret = helpKeys.get(0).trim();
            if (!ret.isEmpty() && ret.length() <= 40) {
                return ret;
            }
        }
        return "";
    }
    
	protected DataList menu(boolean isFavorite, boolean pageWatched, boolean subpagesWatched, boolean gitlab, boolean isCustomerVersion) {
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
		
    	menuitem(i, model.get("movelink").toString(), "fa-arrow-circle-right", n("movePage"));
    	menuitem(i, model.get("deletelink").toString(), "fa-trash", n("deletePage") + "...");
    	return i;
    }
    
    protected void additionalMenuItems(DataList i) { //
	}

	protected void menuitem(DataList menuitems, String link, String icon, String label) {
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
	
	public static String tree(SeitenSO seiten, String lang, String currentSeiteId) {
		String ret = "<ul>";
		for (SeiteSO seite : seiten) {
			if (seite.hasContent(lang) <= 0) {
				continue;
			}
			String a = "";
			if (currentSeiteId.equals(seite.getId())) {
				a = " class=\"treeActivePage\"";
			}
			BookSO book = seite.getBook();
			ret += "<li><a" + a + " href=\"/s/" + Escaper.esc(book.getWorkspace().getBranch()) + "/"
					+ Escaper.esc(book.getBook().getFolder()) + "/" + Escaper.esc(seite.getId()) + "\">"
					+ seite.getSeite().getTitle().getString(lang) + "</a></li>\n";
			if (!seite.getSeiten().isEmpty()) { // TODO <- visible
				ret += tree(seite.getSeiten(), lang, currentSeiteId);
			}
		}
		ret += "</ul>\n";
		return ret;
	}
}
