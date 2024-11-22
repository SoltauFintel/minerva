package minerva.book;

import java.util.ArrayList;
import java.util.List;

import minerva.base.NLS;
import minerva.comment.SeiteCommentService2;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.Visible;
import minerva.mask.FeatureFields;
import minerva.mask.FeatureFieldsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import ohhtml.base.Escaper;

/**
 * Pages tree for book page
 */
public class Gliederung {
	private final BookSO book;
	private final String guiLang;
	private final String lang;
	private final boolean allPages;
	
	public Gliederung(BookSO book, String lang, boolean allPages) {
		this.book = book;
		this.guiLang = book.getWorkspace().getUser().getGuiLanguage();
		this.lang = lang;
		this.allPages = allPages;
	}
	
	public String build() {
		StringBuilder gliederung = new StringBuilder();
		fillSeiten(book.getSeiten(), book.getBook().isSorted(), gliederung);
		return gliederung.toString();
	}

	private void fillSeiten(SeitenSO seiten, boolean sorted, StringBuilder gliederung) {
        String branch = book.getWorkspace().getBranch();
        String bookFolder = book.getBook().getFolder();
        // Wegen der Rekursion ist eine Template-Datei nicht sinnvoll.
        gliederung.append("<ul>\n");
        String hasComment    = " <i class=\"fa fa-comment-o has-comment\" title=\"" + n("hasComment") + "\"></i>";
        String hasCommentForMe = " <i class=\"fa fa-comment has-comment\" title=\"" + n("hasComment") + "\"></i>";
        List<SeiteSO> seitenII = new ArrayList<>(); // needed for indexed iteration
		for (SeiteSO seite : seiten) {
			seitenII.add(seite);
		}
		SeiteSichtbar ssc = new SeiteSichtbar(book.getWorkspace(), lang);
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
            String link = "/s/" + branch + "/" + bookFolder + "/" + Escaper.esc(seite.getSeite().getId());
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
            gliederung.append(Escaper.esc(title));
            gliederung.append("</a>");
            if (showTags(i, seitenII, lang)) {
				seite.getSeite().getTags().stream().sorted().forEach(tag ->
					gliederung.append(" <span class=\"label label-tag\"><i class=\"fa fa-tag\"></i> " + tag + "</span>"));
            }
            int state = new SeiteCommentService2(seite).getCommentState(book.getWorkspace().getUser().getLogin());
            if (state > 0) {
                gliederung.append(state == 2 ? hasCommentForMe : hasComment);
            }
            if (seite.isFeatureTree()) {
				String p = "", info = null, b = null;
				if (seite.hasFt_tag()) {
					b = "f";
					p = "/" + seite.getId();
					info = "Features";
				} else if ("Schnittstellen".equals(seite.getTitle())) {
					b = "sch";
					info = seite.getTitle();
				}
				if (info != null) {
					gliederung.append(" <a href=\"/" + b + "/" + branch + "/" + bookFolder + p
							+ "\"><i class=\"fa fa-table greenbook ml05\" title=\"" + info + "\"></i></a>");
				}
            }
            if (seite.isFeatureTree()) {
            	FeatureFields ff = new FeatureFieldsService().get(seite);
            	if (seite.getBook().getUserRealName().equals(ff.get("responsible"))) {
            		gliederung.append(" <i class=\"fa fa-user ml05 commentByMe\" title=\"" + n("iAmResponsible") + "\"></i>");
            	}
            }
            gliederung.append("</li>\n");
            
            if (seite.isFeatureTree() && seite.checkSubfeaturesLimit()) {
                continue;
            }
            fillSeiten(seite.getSeiten(), true, gliederung); // recursive
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

	private String n(String key) {
		return NLS.get(guiLang, key);
	}
}
