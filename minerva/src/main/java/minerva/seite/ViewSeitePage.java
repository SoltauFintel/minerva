package minerva.seite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.base.Uptodatecheck;
import minerva.book.BookPage;
import minerva.comment.SeiteCommentService2;
import minerva.image.FixHttpImage;
import minerva.mask.FeatureFieldsHtmlFactory;
import minerva.mask.FeatureFieldsService;
import minerva.mask.MaskAndDataFields;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.user.User;
import minerva.user.UserAccess;

public class ViewSeitePage extends SPage implements Uptodatecheck {
    private String mindmapJson;
    
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
            if (foundInOtherBook()) return;
            Logger.error("Page not found: " + id);
            render = false;
            ctx.redirect("/message?m=1");
            return;
        }
        if (isOneLang()) {
            langs = BookPage.oneLang(model, book);
        }
        execute2();
    }

    protected void execute2() {
        User u = user.getFreshUser();
        if (book.isFeatureTree() && !"de".equals(u.getPageLanguage())) {
        	u.setPageLanguage("de");
        }
        seite.forceReloadIfCheap();
        fillLanguageSpecifics(u);
        Seite _seite = seite.getSeite();
        simpleVars(u, _seite);
        subpages();
        new MaskAndDataFields(seite).customersMultiselect(model);
        commentsSize();
        PageChange change = seite.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            fillLastChange(seite, change, esc(n("lastChangeInfo")), model);
        }
        putSize("helpKeysSize", _seite.getHelpKeys());
        String oneHelpKey = getOneHelpKey(_seite.getHelpKeys());
        put("oneHelpKey", esc(oneHelpKey));
        put("hasOneHelpKey", !oneHelpKey.isEmpty());
        boolean isFavorite = u.getFavorites().contains(id);
        put("isFavorite", isFavorite);
        boolean pageWatched = u.getWatchlist().contains(id);
        put("pageWatched", pageWatched);
        boolean subpagesWatched = u.getWatchlist().contains(id + "+");
        put("subpagesWatched", subpagesWatched);
        put("ctrlS", n("ctrlS"));
        levellist("levellist", _seite.getTocHeadingsLevels());
        levellist("levellist2", _seite.getTocSubpagesLevels());
        featureTree();
        editorComponent();
        header(modifyHeader(seite.getTitle()));
        fillLinks(branch, bookFolder, id, seite, _seite, u.getPageLanguage());
        menu(isFavorite, pageWatched, subpagesWatched,
                MinervaWebapp.factory().getConfig().isGitlab(), MinervaWebapp.factory().isCustomerVersion()); // möglichst spät aufrufen
        Logger.info(u.getLogin() + " | " + seite.getBook().getWorkspace().getBranch() + " | "
                + seite.getTitle() + " | " + u.getPageLanguage());
    }

    private void simpleVars(User u, Seite _seite) {
        fillTags(_seite);
        putSize("tagsSize", seite.getSeite().getTags());
        put("book", bookFolder);
        put("id", id);
        put("parentId", esc(_seite.getParentId()));
        putInt("position", _seite.getPosition());
        putInt("version", _seite.getVersion());
        put("bookTitle", esc(seite.getBook().getBook().getTitle().getString(u.getPageLanguage()))); // bin unsicher
        put("isPublicBook", !seite.isNotPublic());
        put("isInternalBook", seite.isInternal());
        put("isFeatureTree", seite.isFeatureTree());
        put("newPage", n(book.isFeatureTree() ? "newFeature" : "newPage"));
        put("Sortierung", n(_seite.isSorted() ? "alfaSorted" : "manuSorted"));
        put("isSorted", _seite.isSorted());
        put("hasAbsoluteUrlImage", new FixHttpImage().hasAbsoluteUrlImage(seite, langs));
        put("featureFields", FeatureFieldsHtmlFactory.FACTORY.build(seite, false).html());
        put("editorsNote", esc(_seite.getEditorsNote()));
        put("editorsNoteBR", esc(_seite.getEditorsNote()).replace("\n", "<br/>"));
        put("hasEditorsNote", !StringService.isNullOrEmpty(_seite.getEditorsNote()));
        String watchers = new WatchersService(seite).getWatchers();
        put("watchers", esc(watchers));
        put("hasWatchers", !watchers.isEmpty());
    }

    private void featureTree() {
        DataList list = list("features");
        if (book.isFeatureTree()) {
            put("hasLeftArea", false);
            put("leftAreaContent", "");
            mindmap();
        } else {
            put("hasLeftArea", true);
            put("leftAreaContent", getTreeHTML(seite));
            put("mindmapData", "");
            new FeatureFieldsService().getFeaturesForSeite(id, workspace).forEach(f -> list.add().put("link", esc(f.seiteId)).put("title", esc(f.title)));
        }
        put("showFeatures", !list.isEmpty());
    }

    private void commentsSize() {
        String cosi = new SeiteCommentService2(seite).getCommentsSizeText(user.getLogin());
        boolean forMe = cosi.startsWith("*");
        if (forMe) {
            cosi = cosi.substring(1);
        }
        put("commentsSize", cosi);
        put("commentsForMe", forMe);
    }

    private void fillLanguageSpecifics(User user) {
        Seite _seite = seite.getSeite();
        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            map.put("LANG", lang.toUpperCase());
            map.put("lang", lang);
            map.put("editorLanguage", lang);
            map.put("onloadExtra",
                    "document.getElementById('titel" + lang.toUpperCase()
                            + "').value = localStorage.getItem('error_titel" + lang.toUpperCase() + "." + _seite.getId() + "');\r\n"
                            + "localStorage.removeItem('error_titel" + lang.toUpperCase() + "." + _seite.getId() + "');\r\n");
            String titel = _seite.getTitle().getString(lang);
            map.put("realtitel", esc(titel));
            if (titel.isBlank()) {
                titel = "without title #" + _seite.getId();
            }
            map.put("titel", esc(titel));
            TocMacro macro = new TocMacro(seite.getTocMacroPage(), "-", lang, "");
            map.put("content", macro.transform(seite.getContent().getString(lang)));
            map.put("toc", macro.getTOC()); // no esc, after transform()
            map.put("active", lang.equals(user.getPageLanguage()));
            fillBreadcrumbs(lang, map.list("breadcrumbs"));
            map.putInt("subpagesSize", fillSubpages(seite, seite.getSeiten(), lang, map.list("subpages"),
                    branch, bookFolder, false));
        }
    }
    
    private void subpages() {
        if (seite.isFeatureTree() && seite.checkSubfeaturesLimit()) {
            put("hasSubPages", false);
            put("hasPositionlink", false);
        } else {
            put("hasSubPages", !seite.getSeiten().isEmpty());
            put("hasPositionlink", seite.getSeiten().size() > 1);
        }
    }

    private void editorComponent() {
        put("bigEditor", true);
        
        String postExtra = "";
        for (String lang : langs) {
            postExtra += "titel" + lang.toUpperCase() + ": document.getElementById('titel" + lang.toUpperCase() + "').value,\r\n";
        }
        postExtra += "comment: document.getElementById('comment').value,\r\n";
        put("postExtra", postExtra);

        String postFailExtra = "";
        for (String lang : langs) {
            postFailExtra += "localStorage.setItem('error_titel" + lang.toUpperCase() + "." + id
                    + "', document.getElementById('titel" + lang.toUpperCase() + "').value);\r\n";
        }
        postFailExtra += "localStorage.setItem('error_delta." + id + "', document.getElementById('comment').value);\r\n";
        put("postFailExtra", postFailExtra);
        
        put("errorName", "editor");
        put("saveError", n("saveError"));
        put("onloadExtra", "document.getElementById('comment').value = localStorage.getItem('error_delta." + id
                + "');\r\n" + "localStorage.removeItem('error_delta." + id + "');\r\n");
    }
    
    private String getTreeHTML(SeiteSO seiteSO) {
        String html = "";
        for (String lang : langs) {
            String hidden = lang.equals(user.getPageLanguage()) ? "" : " hidden";
            String tree = tree(seiteSO.getBook().getSeiten(), lang, seite.getId());
            html += "<div id=\"tree_" + lang + "\"" + hidden + ">" + tree + "</div>";
        }
        return html;
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
    
    static int fillSubpages(SeiteSO seite, SeitenSO seiten, String lang, DataList subpages, String branch, String bookFolder,
            boolean showAllPages) {
        int n = 0;
        if (seite == null
                || !seite.isFeatureTree()
                || !seite.checkSubfeaturesLimit()) {
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
        put("duplicatelink", withSeiteId + "/duplicate");
        put("movelink", withSeiteId + "/move-select-target");
        put("deletelink", withSeiteId + "/delete");
        
        // Edit
        put("editlink", "/s-edit/" + branch + "/" + bookFolder + "/" + id);
        put("imageuploadlink", "/s-image-upload/" + branch + "/" + bookFolder + "/" + id);
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
            ret2 += "$(\"#tree_" + lang + "\").attr(\"hidden\", lang != \"" + lang + "\")\n";
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
            String title = b.getTitle().getString(lang);
            if (title.isBlank()) {
                title = "without title";
            }
            map.put("title", esc(title));
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
        
        if (isAdmin) {
            menuitem(i, viewlink + "/html", "fa-code", n("editHTML"));
        }
        menuitem(i, "/w/" + esc(branch) + "/export?seite=" + u(seite.getId()), "fa-upload", n("exportPage"));
        menuitem(i, model.get("duplicatelink").toString(), "fa-copy", n("duplicatePage"));
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
        List<TreeItem> treeItems = seiten.getTreeItems(lang, currentSeiteId, null);
        return tree2(treeItems, "", true);
    }
    
    private static String tree2(List<TreeItem> treeItems, String id, boolean expanded) {
        String ret = "<ul id=\"P_" + id + "\" class=\"pagetree\"";
        if (id.isEmpty() || expanded) {
            ret += ">";
        } else {
            ret += " style=\"display:none;\">";
        }
        for (int i = 0; i < treeItems.size(); i++) {
			TreeItem seite = treeItems.get(i);
            String aClass = "";
            if (seite.hasContent() == 2) {
                aClass = " class=\"noContent\"";
            }
            if (seite.isCurrent()) {
                if (aClass.isEmpty()) {
                    aClass = " class=\"treeActivePage\"";
                } else {
                    aClass = " class=\"noContent treeActivePage\"";
                }
            }
            String icon = "<i class=\"fa fa-file-o\" style=\"color: #666;\"></i> ";
            boolean hasVisibleSubpages = false;
            for (TreeItem subpage : seite.getSubitems()) {
                if (subpage.hasContent() > 0) {
                    hasVisibleSubpages = true;
                    icon = "<a onclick=\"treeclick('P_" + seite.getId() + "')\" class=\"tci\"><i id=\"iP_" +
                            seite.getId() + "\" class=\"fa " + (seite.isExpanded() ? "fa-caret-down" : "fa-caret-right") + "\" style=\"font-size: 15pt;"
                            + (seite.isExpanded() ? "" : " padding-right: 4px;") + "\"></i></a> ";
                    break;
                }
            }
            ret += "<li><nobr>" + icon + "<a" + aClass + " href=\"" + seite.getLink() + "\">" + seite.getTitle() + "</a>";
            if (showTags(i, treeItems)) {
            	ret += seite.getTags().stream().sorted().map(tag ->
            			" <span class=\"label label-tag\" style=\"padding-left: 1.4em;\"><i class=\"fa fa-tag\"></i> " + tag + "</span>")
            			.collect(Collectors.joining());
            }
            ret += "</nobr></li>\n";
            if (hasVisibleSubpages) {
                ret += tree2(seite.getSubitems(), seite.getId(), seite.isExpanded()); // recursive
            }
        }
        ret += "</ul>\n";
        return ret;
    }
    
	private static boolean showTags(int x, List<TreeItem> seiten) {
		String xt = seiten.get(x).getTitle();
		for (int i = 0; i < seiten.size(); i++) {
			if (i != x && seiten.get(i).getTitle().equals(xt)) {
				return true;
			}
		}
		return false;
	}

    private boolean foundInOtherBook() {
        for (BookSO book : workspace.getBooks()) {
            if (!book.getBook().getFolder().equals(bookFolder)) {
                SeiteSO s = book._seiteById(id);
                if (s != null) {
                    String url = "/s/" + esc(branch) + "/" + esc(book.getBook().getFolder()) + "/" + esc(id);
                    Logger.warn(ctx.path() + ": page does not exist. Found page in another book. Redirecting to: " + url);
                    render = false;
                    ctx.redirect(url);
                    return true;
                }
            }
        }
        return false;
    }
    
    private void mindmap() {
        List<MME> list = new ArrayList<>();
        MME root, parent;
        list.add(root = new MME(seite.getTitle(), "type-a"));
        if (!seite.checkSubfeaturesLimit()) {
            for (SeiteSO sub : seite.getSeiten()) {
                list.add(parent = new MME(sub.getId(), root, sub.getTitle()));
                if (sub.getSeiten().size() < 10) {
                    for (SeiteSO sub2 : sub.getSeiten()) {
                        list.add(new MME(sub2.getId(), parent, sub2.getTitle()));
                    }
                }
            }
        }
        mindmapJson = "";
        add(list, i -> i.id == null, false);
        add(list, i -> i.id != null && i.parentId == null, true);
        put("mindmapData", mindmapJson);
    }
    
    private void add(List<MME> list, Predicate<MME> func, boolean addSub) {
        for (MME i : list) {
            if (func.test(i)) {
                mindmapJson += "{id: " + a(i.id) //
                        + ", parentId: " + a(i.parentId) //
                        + ", text: " + a(i.text) //
                        + (i.type == null ? "" : (", type: \"" + i.type + "\"")) //
                        + "},\n";
                if (addSub) {
                    add(list, j -> j.parentId != null && j.parentId.equals(i.id), true);
                }
            }
        }
    }
    
    private String a(String a) {
        return a == null ? "null" : "\"" + a.replace("\"", "\\\"") + "\"";
    }
}
