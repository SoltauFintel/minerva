package minerva.book;

import java.util.ArrayList;
import java.util.List;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import github.soltaufintel.amalia.web.action.Escaper;
import minerva.MinervaWebapp;
import minerva.base.DeliverHtmlContent;
import minerva.base.Uptodatecheck;
import minerva.comment.SeiteCommentService2;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.Visible;
import minerva.mask.FeatureFields;
import minerva.mask.FeatureFieldsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.seite.PageTree;
import minerva.seite.ViewSeitePage;

public class BookPage extends BPage implements Uptodatecheck {
    public static DeliverHtmlContent<BookSO> additionalButtons = i -> "";
    
    @Override
    protected void execute() {
        boolean allPages = user.getUser().isShowAllPages();
        String guiLanguage = user.getGuiLanguage();
        String pageLanguage = user.getPageLanguage();
        if (book.isFeatureTree() && !"de".equals(pageLanguage)) {
            user.getUser().setPageLanguage("de");
        }
        
        setJQueryObenPageMode();
        String title = book.getBook().getTitle().getString(guiLanguage);
        put("header", esc(title));
        put("title", esc(title.toLowerCase().contains("buch") ? title : title + " (Buch)"));
        put("allPages", allPages);
        put("hasLeftArea", true);
        put("leftAreaContent", new PageTree().getHTML(book.getSeiten(), langs, null, pageLanguage));
        if (isOneLang()) {
            langs = oneLang(model, book);
        }
        put("isFeatureTree", book.isFeatureTree());
        put("positionlink", booklink + "/order");
        put("sortlink", booklink + "/sort");
        put("hasPositionlink", book.getSeiten().size() > 1);
        boolean sorted = book.getBook().isSorted();
        put("isSorted", sorted);
        put("Sortierung", n(sorted ? "alfaSorted" : "manuSorted"));
        put("newPage", n(book.isFeatureTree() ? "newFeature" : "newPage"));
        put("additionalButtons", additionalButtons.getHTML(book));
        put("hasPrevlink", false);
        boolean hasSeiten = !book.getSeiten().isEmpty();
        put("hasNextlink", hasSeiten);
        if (hasSeiten) {
            put("nextlink", "/s/" + branch + "/" + book.getBook().getFolder() + "/" + book.getSeiten().get(0).getId());
        }
        SeiteSO change = book.getLastChange();
        put("hasLastChange", change != null);
        if (change != null) {
            ViewSeitePage.fillLastChange(change, change.getLastChange(), n("lastChangeInfoForBook")/*no esc()*/, model);
        }

        DataList list = list("languages");
        for (String lang : langs) {
            DataMap map = list.add();
            StringBuilder gliederung = new StringBuilder();
            fillSeiten(branch, bookFolder, book.getSeiten(), lang, allPages, book.getBook().isSorted(), gliederung);
            map.put("lang", lang);
            map.put("LANG", lang.toUpperCase());
            map.put("gliederung", gliederung.toString());
            map.put("active", pageLanguage.equals(lang));
            String bookTitle = book.getBook().getTitle().getString(lang);
            if (bookTitle.isBlank()) {
                bookTitle = book.getBook().getFolder();
            }
            map.put("bookTitle", esc(bookTitle));
        }
    }
    
    public static List<String> oneLang(DataMap model, BookSO book) {
        model.put("hasBook", false);
        model.put("hasMenuItems", true);
        DataList menuItems = model.list("menuItems");
        DataMap map = menuItems.add();
        map.put("link", "/b/" + book.getWorkspace().getBranch() + "/" + book.getBook().getFolder());
        map.put("title", Escaper.esc(book.getTitle()));

        return MinervaWebapp.factory().getConfig().getOneLang();
    }

    private void fillSeiten(String branch, String bookFolder, SeitenSO seiten, String lang, boolean allPages,
            boolean sorted, StringBuilder gliederung) {
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        String hasComment    = " <i class=\"fa fa-comment-o has-comment\" title=\"" + n("hasComment") + "\"></i>";
        String hasCommentForMe = " <i class=\"fa fa-comment has-comment\" title=\"" + n("hasComment") + "\"></i>";
        List<SeiteSO> seitenII = new ArrayList<>(); // needed for indexed iteration
		for (SeiteSO seite : seiten) {
			seitenII.add(seite);
		}
		SeiteSichtbar ssc = new SeiteSichtbar(workspace, lang);
		ssc.setShowAllPages(allPages);
		for (int i = 0; i < seitenII.size(); i++) {
			SeiteSO seite = seitenII.get(i);
			Visible visible = ssc.getVisibleResult(seite);
			if (!visible.isVisible()) {
			    continue;
			}
        	String trueTitle = seite.getSeite().getTitle().getString(lang);
            String title = trueTitle;
            if (title.isBlank()) {
                title = "without title #" + seite.getId();
            }
            String link = "/s/" + branch + "/" + bookFolder + "/" + esc(seite.getSeite().getId());
            String nc = "";
            if (visible.isShowAllPages()) {
            	nc = " class=\"hiddenPage\""; // red
            } else if (visible.hasSubpages()) {
            	nc = " class=\"noContent\"";  // grey
            }
            gliederung.append("\t<li id=\"");
            gliederung.append(seite.getId());
            gliederung.append("\"><a href=\"");
            gliederung.append(link);
            gliederung.append("\"" + nc + ">");
            gliederung.append(esc(title));
            gliederung.append("</a>");
            if (showTags(i, seitenII, lang)) {
				seite.getSeite().getTags().stream().sorted().forEach(tag ->
					gliederung.append(" <span class=\"label label-tag\"><i class=\"fa fa-tag\"></i> " + tag + "</span>"));
            }
            int state = new SeiteCommentService2(seite).getCommentState(user.getLogin());
            if (state > 0) {
                gliederung.append(state == 2 ? hasCommentForMe : hasComment);
            }
            if (seite.isFeatureTree() && seite.hasFt_tag()) {
				gliederung.append(" <a href=\"/f/" + branch + "/" + bookFolder + "/" + seite.getId()
						+ "\"><i class=\"fa fa-table greenbook ml05\" title=\"Features\"></i></a>");
            }
            if (seite.isFeatureTree()) {
            	FeatureFields ff = new FeatureFieldsService().get(seite);
            	if (seite.getBook().getWorkspace().getUser().getUser().getRealName().equals(ff.get("responsible"))) {
            		gliederung.append(" <i class=\"fa fa-user ml05 commentByMe\" title=\"" + n("iAmResponsible") + "\"></i>");
            	}
            }
            gliederung.append("</li>\n");
            
            if (seite.isFeatureTree() && seite.checkSubfeaturesLimit()) {
                continue;
            }
            fillSeiten(branch, bookFolder, seite.getSeiten(), lang, allPages, true, gliederung); // recursive
        }
        gliederung.append("</ul>\n");
    }

	private boolean showTags(int x, List<SeiteSO> seiten, String lang) {
		String xt = seiten.get(x).getSeite().getTitle().getString(lang);
		for (int i = 0; i < seiten.size(); i++) {
			if (i != x && seiten.get(i).getSeite().getTitle().getString(lang).equals(xt)) {
				return true;
			}
		}
		return false;
	}
}
