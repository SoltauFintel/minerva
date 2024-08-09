package minerva.exclusions;

import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import minerva.model.SeiteSO;

public class SeiteSichtbar {
    private final SeiteSO seite;
    private final SeiteSichtbarContext context;
    
    SeiteSichtbar(SeiteSO seite, SeiteSichtbarContext context) {
        this.seite = seite;
        this.context = context;
    }

    Visible getVisibleResult() {
    	return getVisibleResult(seite, context);
    }

    // main part
    private static Visible getVisibleResult(SeiteSO seite, SeiteSichtbarContext context) {
        if (!isAccessible(seite.getSeite().getTags(), context)) {
        	// Es ist ein Kunde gesetzt und dessen Ausschlüsse-tags verbieten den Zugriff auf die Seite.
        	return new Visible(false);
        } else {
            // Wenn es für mind. eine Sprache nicht leer ist, dann ist die Seite sichtbar.
            for (String lang : context.getLanguages()) {
            	if (!isEmpty(seite, lang)) {
                    return new Visible(true);
                }
            }
            // Die Seite ist leer und daher eigentlich nicht sichtbar.
            // Wenn es aber mindestens eine nicht-leere Unterseite gibt, dann ist das Ergebnis hasSubpages statt nicht-sichtbar.
            for (SeiteSO sub : seite.getSeiten()) {
            	if (getVisibleResult(sub, context).isVisible()) { // recursive
            		return new Visible(true, true, false);
            	}
            }
            // Seite nicht sichtbar oder show-all-pages-mode aktiv.
        	return new Visible(context.isShowAllPages(), false, context.isShowAllPages());
        }
    }

    private static boolean isAccessible(Set<String> tags, SeiteSichtbarContext context) {
        if (!context.hasCustomer()) {
            return true;
        }
        boolean ret = true;
        boolean voteForON = false;
        List<String> exclusionsTags = context.getExclusions().getTags(context.getCustomer().toLowerCase());
        for (String tag : tags) {
            LabelClass v;
            if (context.isPdfTag(tag)) {
                v = LabelClass.OFF;
            } else {
                v = contains(tag, exclusionsTags);
            }
            if (v == LabelClass.ON) {
                voteForON = true;
            } else if (v == LabelClass.OFF) {
                return false;
            } else if (v == LabelClass.NORMAL) {
                ret = false;
            }
        }
        return voteForON || ret;
    }

    private enum LabelClass {
        /** normal exclusion label */
        NORMAL,
        /**
         * negative list. if this exclusion label occurs then always return "page is not
         * accessible"
         */
        OFF,
        /**
         * positive list. if this label occurs then override NORMAL LabelClass but not
         * OFF LabelClass
         */
        ON,
        /** does not contain */
        NOT_IN;
    }

    private static LabelClass contains(String tag, List<String> exclusionsTags) {
        if (exclusionsTags != null) {
            for (String i : exclusionsTags) {
                if (i.equalsIgnoreCase("+" + tag)) {
                    return LabelClass.ON;
                } else if (i.equalsIgnoreCase("-" + tag)) {
                    return LabelClass.OFF;
                } else if (i.equalsIgnoreCase(tag)) {
                    return LabelClass.NORMAL;
                }
            }
        }
        return LabelClass.NOT_IN;
    }

    private static boolean isEmpty(SeiteSO seite, String language) {
        return !seite.getBook().isFeatureTree() // Leere Seiten sind im Feature Tree Standard und sollen nicht ausgeblendet werden.
        		&& (seite.getSeite().getTags().contains("autolink") // autolink-Seiten soll standardmäßig versteckt werden.
        				|| contentIsEmpty(seite, language));
        
    }

    public static boolean contentIsEmpty(SeiteSO seite, String language) {
        // In theory, this approach is a bit expensive since all content must be loaded and must be parsed.
        // However in practice it takes less than 0.4 seconds on the first call.
        try {
            String html = seite.getContent().getString(language);
            Document doc = Jsoup.parse(html);
            Elements body = doc.select("body");
            return body == null || body.isEmpty() || body.get(0).childrenSize() == 0;
        } catch (Exception e) {
            Logger.error(e);
            return false; // Im Fehlerfall wie eine nicht-leere Seite behandeln, d.h. die Seite würde dann angezeigt werden.
            // Das soll verhindern, dass im Fehlerfall eine Seite gar nicht mehr zugreifbar wäre.
        }
    }
}
