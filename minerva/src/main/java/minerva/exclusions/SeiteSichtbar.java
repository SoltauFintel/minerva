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

    /**
     * cheap constructor, should be used for many executions
     * 
     * @param seite   -
     * @param context -
     */
    public SeiteSichtbar(SeiteSO seite, SeiteSichtbarContext context) {
        this.seite = seite;
        this.context = context;
    }

    /**
     * expensive constructor, often used for a single execution
     * 
     * @param seite
     */
    public SeiteSichtbar(SeiteSO seite) {
        this(seite, new SeiteSichtbarContext(seite.getBook().getWorkspace()));
    }

    public boolean isVisible() {
        boolean ret = isAccessible(seite.getSeite().getTags(), context);
        if (ret) {
            // Wenn es für mind. eine Sprache nicht leer ist, dann ist die Seite sichtbar.
            for (String lang : context.getLanguages()) {
                if (hasContent(seite, lang, false) != HasContentEnum.EMPTY) {
                    return true;
                }
            }
            ret = false;
        }
        return ret;
    }

    public boolean hasSubpages(String lang) {
        return hasContent(seite, lang, false) == HasContentEnum.EMPTY_BUT_HAS_NONEMPTY_SUBPAGES;
    }

    public static boolean isEmptyOrHasSubpages(SeiteSO seite, String lang) {
        HasContentEnum hc = hasContent(seite, lang, false);
        return hc == HasContentEnum.EMPTY || hc == HasContentEnum.EMPTY_BUT_HAS_NONEMPTY_SUBPAGES;
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

    private static HasContentEnum hasContent(SeiteSO seite, String lang, boolean returnError) {
        if (seite.getBook().isFeatureTree()) {
            return HasContentEnum.NOT_EMPTY;
        } else if (seite.getSeite().getTags().contains("autolink")) {
            return HasContentEnum.EMPTY;
        }
        return hasContentR(seite, lang, returnError);
    }

    public static HasContentEnum hasContentR(SeiteSO seite, String lang, boolean returnError) {
        // In theory, this approach is a bit expensive since all content must be loaded and must be parsed.
        // However in practice it takes less than 0.4 seconds on the first call.
        try {
            String html = seite.getContent().getString(lang);
            Document doc = Jsoup.parse(html);
            Elements body = doc.select("body");
            if (body != null && !body.isEmpty() && body.get(0).childrenSize() > 0) {
                return HasContentEnum.NOT_EMPTY;
            }
            for (SeiteSO sub : seite.getSeiten()) {
                if (hasContentR(sub, lang, returnError) != HasContentEnum.EMPTY) { // recursive
                    return HasContentEnum.EMPTY_BUT_HAS_NONEMPTY_SUBPAGES;
                }
            }
            return HasContentEnum.EMPTY;
        } catch (Exception e) {
            Logger.error(e);
            return returnError ? HasContentEnum.ERROR : HasContentEnum.NOT_EMPTY;
        }
    }
}
