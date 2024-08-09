package minerva.exclusions;

import java.util.ArrayList;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.config.MinervaOptions;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

// Dieses Objekt lebt nur für die Dauer der Anfrage. Wenn viele Seiten auf einmal zu prüfen sind, lebt es halt so lange.
// Es klebt aber nicht am Workspace.

public class SeiteSichtbarContext {
    private final WorkspaceSO workspace; // TODO muss ich den speichern?
    private final Exclusions exclusions;
    private final List<String> languages = new ArrayList<>();
    private String customer;
    private boolean showAllPages;
    private final String[] pdfTags;

    // TODO context mit ungleich 1 language  .... Macht das Sinn? Wann genutzt?

    public SeiteSichtbarContext(WorkspaceSO workspace) {
        this(workspace, MinervaWebapp.factory().getLanguages());
    }

    public SeiteSichtbarContext(WorkspaceSO workspace, List<String> languages) {
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
    public SeiteSichtbarContext(WorkspaceSO workspace, String customer, boolean pdfExport, String language) {
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
    
    public SeiteSichtbarContext withLanguage(String language) {
        languages.clear();
        languages.add(language);
        return this;
    }
    
    public boolean isVisible(SeiteSO seite) {
    	return getVisibleResult(seite).isVisible();
    }
    
    public Visible getVisibleResult(SeiteSO seite) {
    	return new SeiteSichtbar(seite, this).getVisibleResult();
    }
}
