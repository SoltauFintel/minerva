package minerva.exclusions;

import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.user.CustomerMode;
import minerva.user.User;
import ohhtml.Exclusions;

/**
 * Seite sichtbar Kontext
 *
 * <p>Dieses Objekt lebt nur für die Dauer der Anfrage. Wenn viele Seiten auf einmal zu prüfen sind, lebt es halt so lange.
 * Es klebt aber nicht am Workspace.</p>
 */
public class SeiteSichtbar {
    private final Exclusions exclusions;
    private final List<String> languages;
    private CustomerMode customerMode;
    private boolean showAllPages;
    private final String[] pdfTags;
    
    /**
     * copy constructor with set language
     * @param ss -
     * @param language -
     */
    public SeiteSichtbar(SeiteSichtbar ss, String language) {
        this.exclusions = ss.exclusions;
        this.languages = List.of(language);
        this.customerMode = ss.customerMode;
        this.showAllPages = ss.showAllPages;
        this.pdfTags = ss.pdfTags;
    }

    /**
     * constructor: all languages
     * @param workspace -
     */
    public SeiteSichtbar(WorkspaceSO workspace) {
        this(workspace, MinervaWebapp.factory().getLanguages()); // alle Sprachen: macht Sinn bei Reorder und beim Navigieren
    }

    /**
     * constructor: one language
     * @param workspace -
     * @param language e.g. "de"
     */
    public SeiteSichtbar(WorkspaceSO workspace, String language) {
        this(workspace, List.of(language));
    }
    
    private SeiteSichtbar(WorkspaceSO workspace, List<String> languages) {
        exclusions = workspace.exclusions();
        this.languages = languages;
        User user = workspace.getUser().getUser();
        customerMode = workspace.getUser().getCustomerMode();
        showAllPages = user.isShowAllPages();
        pdfTags = new String[0];
    }
    
    /**
     * Constructor for export
     * @param workspace -
     * @param customer -
     * @param pdfExport true: PDF export, false: HTML export
     * @param language -
     */
    public SeiteSichtbar(WorkspaceSO workspace, CustomerMode customer, boolean pdfExport, String language) {
        languages = List.of(language);
        exclusions = workspace.exclusions();
        this.customerMode = customer;
        showAllPages = false; // wegen autolink muss das false sein
        if (pdfExport) {
            String tags = MinervaOptions.PDF_TAGS.get(); // nicht_drucken
            if (tags == null) {
                pdfTags = new String[0];
            } else {
                pdfTags = tags.split(",");
            }
        } else {
            pdfTags = new String[0];
        }
    }

    private boolean isPdfTag(String tag) {
        for (String i : pdfTags) {
            if (i.equals(tag)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasCustomer() {
        return customerMode.isActive();
    }
    
    public void setCustomerMode(CustomerMode customerMode) {
        this.customerMode = customerMode;
    }

    public void setShowAllPages(boolean showAllPages) {
        this.showAllPages = showAllPages;
    }
    
    /**
     * @param seite -
     * @return Seite sichtbar?
     */
    public boolean isVisible(SeiteSO seite) {
        return getVisibleResult(seite, this).isVisible();
    }

    /**
     * @param seite -
     * @return Sichtbarkeit der Seite
     */
    public Visible getVisibleResult(SeiteSO seite) {
        return getVisibleResult(seite, this);
    }
    
    // main part
    private static Visible getVisibleResult(SeiteSO seite, SeiteSichtbar context) {
        boolean noTree = isNoTree(seite);
        Accessibility accessibility = isAccessible(seite.getSeite().getTags(), context);
        if (!accessibility.accessible) {
            // Es ist ein Kunde gesetzt und dessen Ausschlüsse-tags verbieten den Zugriff auf die Seite.
            return new Visible(noTree, false, accessibility.reason);
        } else {
            // Wenn es für mind. eine Sprache nicht leer ist, dann ist die Seite sichtbar.
            for (String lang : context.languages) {
                if (!isEmpty(seite, lang)) {
                    return new Visible(noTree, true, ""/*sichtbar weil für Sprache ... nicht leer*/);
                }
            }
            // Die Seite ist leer und daher eigentlich nicht sichtbar.
            // Wenn es aber mindestens eine nicht-leere Unterseite gibt, dann ist das Ergebnis hasSubpages statt nicht-sichtbar.
            for (SeiteSO sub : seite.getSeiten()) {
                if (getVisibleResult(sub, context).isVisible()) { // recursive
                    return new Visible(noTree, true, true, false, ""/*leer, aber dennoch sichtbar weil Unterseite ... sichtbar ist*/);
                }
            }
            // Seite nicht sichtbar oder show-all-pages-mode aktiv.
            return new Visible(noTree, context.showAllPages, false, context.showAllPages,
                    context.showAllPages ? ""/*sichtbar weil alle-Seiten-Modus aktiv*/ : "reasonEMPTY");
        }
    }
    
    // same algo in oh-webapp/Subhtml
    private static boolean isNoTree(SeiteSO seite) {
        if (seite.getSeite().getTags().contains("no-tree")) {
            for (SeiteSO sub : seite.getSeiten()) {
                if (!isNoTree(sub)) { // recursive
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static Accessibility isAccessible(Set<String> tags, SeiteSichtbar context) {
        if (!context.hasCustomer()) {
            return new Accessibility(); // weil Kundenmodus nicht aktiv
        }
        Accessibility ret = new Accessibility(); // weil kann tag es unsichtbar macht
        boolean voteForON = false;
        List<String> exclusionsTags = context.exclusions.getTags(context.customerMode.getCustomer().toLowerCase());
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
                return new Accessibility("reasonOFFTAG|" + tag);
            } else if (v == LabelClass.NORMAL) {
                ret = new Accessibility("reasonTAG|" + tag);
            }
        }
        return voteForON ? new Accessibility() /*wegen 'ON' tag ...*/ : ret;
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
    
    private static class Accessibility {
        final boolean accessible;
        final String reason;

        /** is accessible */
        Accessibility() {
            accessible = true;
            reason = "";
        }
        
        /** is not accessible */
        Accessibility(String reason) {
            accessible = false;
            this.reason = reason;
        }
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
