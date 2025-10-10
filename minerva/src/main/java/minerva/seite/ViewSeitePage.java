package minerva.seite;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.base.DeliverHtmlContent;
import minerva.base.FillModel;
import minerva.base.MinervaMetrics;
import minerva.base.Uptodatecheck;
import minerva.book.BookPage;
import minerva.comment.SeiteCommentService2;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.Visible;
import minerva.image.FixHttpImage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.actions.EditorsNoteModal;
import minerva.seite.helpkeys.VSPTocMacro;
import minerva.user.User;
import minerva.user.UserAccess;
import ohhtml.Thumbnails;
import ohhtml.toc.LocalAnchors;
import ohhtml.toc.TocMacro;

public class ViewSeitePage extends SPage implements Uptodatecheck {
    public static DeliverHtmlContent<SeiteSO> additionalButtons = seite -> "";
    public static DeliverHtmlContent<SeiteSO> featureStatusButtons = seite -> "";
    public static DeliverHtmlContent<SeiteSO> featureFields = seite -> "";
    public static FillModel<SeiteSO, DataList> fillFeaturesList = (seite, list) -> {};
    public static FillModel<SeiteSO, DataMap> customersMultiselect = (seite, model) -> {};
    public static AddFeatures addFeatures = (seite, features) -> {};
    public static PageMenuSupplier menuSupplier = new PageMenuSupplier();
    private String mindmapJson;
    private SeiteSichtbar ssc;
    
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
        pagemode();
        MinervaMetrics.PAGE_VIEW.inc();
    }

    protected void execute2() {
        User u = user.getFreshUser();
        if (book.isFeatureTree() && !"de".equals(u.getPageLanguage())) {
        	u.setPageLanguage("de");
        }
        seite.forceReloadIfCheap();
        ssc = new SeiteSichtbar(workspace);
        Visible vr = ssc.getVisibleResult(seite);
        put("visible", vr.isVisible());
        put("visibleReason", esc(vr.getReason(user.getGuiLanguage())));
        fillLanguageSpecifics(u);
        Seite _seite = seite.getSeite();
        simpleVars(u, _seite);
        customersMultiselect.fill(seite, model);
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

        PageMenuContext pmc = new PageMenuContext(seite, isAdmin, isFavorite, pageWatched, subpagesWatched, model);
		DataList menuitems = model.list("menuitems");
		menuSupplier.getMenuItems(pmc).forEach(item -> item.add(pmc, menuitems)); // möglichst spät aufrufen
        
		Logger.info(seite.getLogLine(u));
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
        put("isFeatureTree", seite.isFeatureTree());
        put("newPage", n(book.isFeatureTree() ? "newFeature" : "newPage"));
        put("Sortierung", n(_seite.isSorted() ? "alfaSorted" : "manuSorted"));
        put("isSorted", _seite.isSorted());
        put("hasAbsoluteUrlImage", new FixHttpImage().hasAbsoluteUrlImage(seite, langs));
        put("featureFields", featureFields.getHTML(seite));
        put("EditorsNoteModal", new EditorsNoteModal(seite)); // component
        put("editorsNoteBR", esc(_seite.getEditorsNote()).replace("\n", "<br/>"));
        put("hasEditorsNote", !StringService.isNullOrEmpty(_seite.getEditorsNote()));
        String watchers = new WatchersService(seite).getWatchers();
        put("watchers", esc(watchers));
        put("hasWatchers", !watchers.isEmpty());
        put("hasAttachments", seite.hasAttachments());
        put("additionalButtons", additionalButtons.getHTML(seite));
    }

    private void featureTree() {
        DataList list = list("features");
        if (book.isFeatureTree()) {
            put("hasLeftArea", false);
            put("leftAreaContent", "");
            mindmap();
            put("editButton2", n("editContent"));
        } else {
            put("hasLeftArea", true);
            put("leftAreaContent", new PageTree().getHTML(seite.getBook().getSeiten(), langs, seite.getId(), user.getPageLanguage()));
            put("mindmapData", "");
            fillFeaturesList.fill(seite, list);
            addFeatures.addFeatures(seite, list);
			list.sort((a, b) -> a.getValue("title").toString().compareToIgnoreCase(b.getValue("title").toString()));
            put("editButton2", n("edit"));
        }
        put("showFeatures", !list.isEmpty());
    }
    
    public interface AddFeatures {
    	void addFeatures(SeiteSO seite, DataList features);
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
        put("guiLanguage", user.getGuiLanguage());
        int errors = 0;
        boolean hasSubPages = false;
        boolean hasPositionlink = false;
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
			TocMacro macro = new VSPTocMacro(seite.getTocMacroPage(), lang);
            macro.setHelpKeysText(n("helpKeys"));
            if (!MinervaWebapp.factory().isCustomerVersion()) {
                macro.setSeite(new SeiteIPageAdapter(seite));
            }
            map.put("content", transformContent(macro, lang, map));
            map.put("active", lang.equals(user.getPageLanguage()));
            if (!MinervaWebapp.factory().isCustomerVersion()) {
                errors += macro.fillHkhErrors(map);
            }
            fillBreadcrumbs(lang, map.list("breadcrumbs"));
            int size = fillSubpages(seite, seite.getSeiten(), lang, map.list("subpages"), branch, bookFolder, new SeiteSichtbar(ssc, lang));
			map.putInt("subpagesSize", size);
			if (size > 0) {
			    hasSubPages = true;
			}
			if (size >= 2 || (seite.isFeatureTree() && seite.getSeiten().size() >= 2)) {
			    hasPositionlink = true;
			}
        }
        put("hasSubPages", hasSubPages);
        put("hasPositionlink", hasPositionlink);
        put("hasErrorsTotal", errors > 0);
        put("page", seite.isPageInFeatureTree()); // normal page in feature tree
    }
    
    protected String transformContent(TocMacro macro, String lang, DataMap map) {
        String html = seite.getContent().getString(lang);
        html = macro.transform(html); // TOC, help keys links
        html = new LocalAnchors().transform(html); // after TocMacro.transform()
        html = Thumbnails.thumbnails(html, seite.getBook().getFolder(), seite.getId(), booklink.replace("/b/", "/s/") + "/");
        map.put("toc", macro.getTOC()); // no esc, after TocMacro.transform()
        return html;
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
    
    public static int fillSubpages(SeiteSO seite, SeitenSO seiten, String lang, DataList subpages, String branch, String bookFolder, SeiteSichtbar ssc) {
        int n = 0;
        if (seite != null && seite.isFeatureTree() && seite.checkSubfeaturesLimit()) {
            // Actually no sub-features display, but pages with tag "page" should be displayed.
            seiten.sort(lang);
            for (SeiteSO sub : seiten) {
                if ((ssc == null || ssc.isVisible(sub)) && sub.isPageInFeatureTree()) {
                    fillSubpage(sub, subpages, lang, branch, bookFolder);
                    n++;
                }
            }
            return n;
        }
        if (seite == null
                || !seite.isFeatureTree()
                || !seite.checkSubfeaturesLimit()) {
            seiten.sort(lang);
            for (SeiteSO sub : seiten) {
                if (ssc == null || ssc.isVisible(sub)) {
                    fillSubpage(sub, subpages, lang, branch, bookFolder);
                    n++;
                }
            }
        }
        return n;
    }
    
    private static void fillSubpage(SeiteSO sub, DataList subpages, String lang, String branch, String bookFolder) {
        DataMap map = subpages.add();
        map.put("id", Escaper.esc(sub.getId()));
        map.put("titel", Escaper.esc(sub.getSeite().getTitle().getString(lang)));
        map.put("viewlink", "/s/" + branch + "/" + bookFolder + "/" + Escaper.esc(sub.getId()));
        map.putInt("position", sub.getSeite().getPosition());
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
        NavigateService nav = new NavigateService(pageLanguage, ssc);
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
		put("editlink", esc((seiteSO.getBook().isReleaseNotes() ? "/s-dont/" : "/s-edit/") + branch + "/" + bookFolder + "/" + id));
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
            NavigateService nav = new NavigateService(lang, null);
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
            NavigateService nav = new NavigateService(lang, null);
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
    
    private boolean foundInOtherBook() {
        for (BookSO book : workspace.getBooks()) {
            if (!book.getBook().getFolder().equals(bookFolder)) {
                SeiteSO s = book._seiteById(id);
                if (s != null) {
                    String url = "/s/" + esc(branch) + "/" + esc(book.getBook().getFolder()) + "/" + esc(id);
                    Logger.info(ctx.path() + ": Page does not exist. Found page in another book. Redirecting to: " + url);
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
                if (!sub.isPageInFeatureTree()) {
                    list.add(parent = new MME(sub.getId(), root, sub.getTitle()));
                    if (sub.getSeiten().size() < 10) {
                        for (SeiteSO sub2 : sub.getSeiten()) {
                            if (!sub2.isPageInFeatureTree()) {
                                list.add(new MME(sub2.getId(), parent, sub2.getTitle()));
                            }
                        }
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
    
    protected void pagemode() {
        setMathMultiselectPageMode();
    }
}
