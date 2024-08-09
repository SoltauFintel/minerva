package minerva.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

// Dieses Objekt lebt nur für die Dauer der Anfrage. Wenn viele Seiten auf einmal zu prüfen sind, lebt es halt so lange.
// Es klebt aber nicht am Workspace.

public class SeiteSichtbar {
    private final WorkspaceSO workspace; // TODO muss ich den speichern?
    private final Exclusions exclusions;
    private final List<String> languages = new ArrayList<>();
    private String customer;
    private boolean showAllPages;
    private final String[] pdfTags;

    // TODO context mit ungleich 1 language  .... Macht das Sinn? Wann genutzt?

    public SeiteSichtbar(WorkspaceSO workspace) {
        this(workspace, MinervaWebapp.factory().getLanguages());
    }

    public SeiteSichtbar(WorkspaceSO workspace, List<String> languages) {
        this.workspace = workspace;
        exclusions = workspace.exclusions();
        this.languages.addAll(languages);
        customer = workspace.getUser().getUser().getCustomerMode();
        showAllPages = workspace.getUser().getUser().isShowAllPages();
        pdfTags = new String[0];
    }

    /**
     * Constructor for export
     * @param workspace -
     * @param customer -
     * @param pdfExport true: PDF export, false: HTML export
     * @param language -
     */
    public SeiteSichtbar(WorkspaceSO workspace, String customer, boolean pdfExport, String language) {
        this.workspace = workspace;
        languages.add(language);
        exclusions = workspace.exclusions();
        this.customer = customer;
        showAllPages = false; // TODO zu klären
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

    // TODO prüfen ob benötigt
    public WorkspaceSO getWorkspace() {
        return workspace;
    }

    public boolean isPdfTag(String tag) {
        for (String i : pdfTags) {
            if (i.equals(tag)) {
                return true;
            }
        }
        return false;
    }
    
    public String getCustomer() {
        return customer;
    }
    
    public boolean hasCustomer() {
        return !StringService.isNullOrEmpty(customer) && !"-".equals(customer);
    }
    
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isShowAllPages() {
        return showAllPages;
    }

    public void setShowAllPages(boolean showAllPages) {
        this.showAllPages = showAllPages;
    }
    
    public Exclusions getExclusions() {
        return exclusions;
    }

    public List<String> getLanguages() {
        return languages;
    }
    
    public SeiteSichtbar withLanguage(String language) {
        languages.clear();
        languages.add(language);
        return this;
    }
    
    public boolean isVisible(SeiteSO seite) {
    	return getVisibleResult(seite, this).isVisible();
    }
    
    public Visible getVisibleResult(SeiteSO seite) {
    	return getVisibleResult(seite, this);
    }
    
    // main part
    private static Visible getVisibleResult(SeiteSO seite, SeiteSichtbar context) {
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

    private static boolean isAccessible(Set<String> tags, SeiteSichtbar context) {
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
