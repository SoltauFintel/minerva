package minerva.book;

import java.util.ArrayList;
import java.util.List;

import minerva.base.DeliverHtmlContent;
import minerva.base.NLS;
import minerva.comment.SeiteCommentService2;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.Visible;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import ohhtml.base.Escaper;

/**
 * Pages tree for book page
 */
public class Gliederung {
	public static DeliverHtmlContent<SeiteSO> append = seite -> "";
	public static DeliverHtmlContent<SeiteSO> recursively = seite -> "true";
	private final BookSO book;
	private final String guiLang;
	private final String lang;
	private final boolean allPages;
	private DeliverHtmlContent<SeiteSO> alternativeAppend;
	
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
        List<SeiteSO> seiten2 = createSeitenCopy(seiten);
		SeiteSichtbar ssc = new SeiteSichtbar(book.getWorkspace(), lang);
		ssc.setShowAllPages(allPages);
		for (int i = 0; i < seiten2.size(); i++) {
			SeiteSO seite = seiten2.get(i);
			Visible visible = ssc.getVisibleResult(seite);
			if (!visible.isVisible()) {
			    continue;
			}
            entry(gliederung, branch, bookFolder, seite, visible);
            tags(gliederung, seiten2, i, seite);
            if (alternativeAppend == null) {
            	comments(gliederung, hasComment, hasCommentForMe, seite);
            	gliederung.append(append.getHTML(seite));
            } else {
            	gliederung.append(alternativeAppend.getHTML(seite));
            }
            gliederung.append("</li>\n");
            
			if ("true".equals(recursively.getHTML(seite))) {
				fillSeiten(seite.getSeiten(), true, gliederung); // recursive
			}
        }
        gliederung.append("</ul>\n");
    }

	private List<SeiteSO> createSeitenCopy(SeitenSO seiten) {
		List<SeiteSO> copy = new ArrayList<>(); // needed for indexed iteration
		for (SeiteSO seite : seiten) {
			copy.add(seite);
		}
		return copy;
	}

	private void entry(StringBuilder gliederung, String branch, String bookFolder, SeiteSO seite, Visible visible) {
        gliederung.append("\t<li id=\"");
		gliederung.append(seite.getId());
		gliederung.append("\"><a href=\"");
		gliederung.append("/s/");
		gliederung.append(branch);
		gliederung.append("/");
		gliederung.append(bookFolder);
		gliederung.append("/");
		gliederung.append(Escaper.esc(seite.getSeite().getId()));
		gliederung.append("\"" + getNC(visible) + ">");
		gliederung.append(Escaper.esc(getTitle(seite)));
		gliederung.append("</a>");
	}

	private String getNC(Visible visible) {
		String nc = "";
		if (visible.isShowAllPages()) {
			nc = " class=\"hiddenPage\""; // red
		} else if (visible.hasSubpages()) {
			nc = " class=\"noContent\"";  // grey
		}
		return nc;
	}

	private String getTitle(SeiteSO seite) {
		String title = seite.getSeite().getTitle().getString(lang);
		if (title.isBlank()) {
		    title = "without title #" + seite.getId();
		}
		return title;
	}

	private void tags(StringBuilder gliederung, List<SeiteSO> seitenII, int i, SeiteSO seite) {
		if (showTags(i, seitenII, lang)) {
			seite.getSeite().getTags().stream().sorted().forEach(tag ->
				gliederung.append(" <span class=\"label label-tag\"><i class=\"fa fa-tag\"></i> " + tag + "</span>"));
		}
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

	private void comments(StringBuilder gliederung, String hasComment, String hasCommentForMe, SeiteSO seite) {
		int state = new SeiteCommentService2(seite).getCommentState(book.getWorkspace().getUser().getLogin());
		if (state > 0) {
		    gliederung.append(state == 2 ? hasCommentForMe : hasComment);
		}
	}

	private String n(String key) {
		return NLS.get(guiLang, key);
	}

	public DeliverHtmlContent<SeiteSO> getAlternativeAppend() {
		return alternativeAppend;
	}

	public void setAlternativeAppend(DeliverHtmlContent<SeiteSO> alternativeAppend) {
		this.alternativeAppend = alternativeAppend;
	}
}
