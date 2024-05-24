package minerva.releasenotes;

import java.util.HashMap;
import java.util.Map;

import minerva.confluence.ConfluencePage2;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class ReleaseNotesContext {
    private final ReleaseNotesConfig config;
    private String releaseId;
    private final BookSO book;
    private final Map<String, String> files = new HashMap<>();
    private ConfluencePage2 releasePage;
    private SeiteSO customerPage;
    private SeiteSO sectionPage;
    private SeiteSO resultingReleasePage;
    private String releaseNumber;
    private String project;
    
    public ReleaseNotesContext(ReleaseNotesConfig config, String releaseId, BookSO book) {
        this.config = config;
        this.releaseId = releaseId;
        this.book = book;
    }

    public ReleaseNotesConfig getConfig() {
        return config;
    }

    public String getSpaceKey() {
        return config.getSpaceKey();
    }

    public String getRootTitle() {
        return config.getRootTitle();
    }

    public String getReleaseId() {
        return releaseId;
    }

    public BookSO getBook() {
        return book;
    }

    public String getLang() {
        return config.getLanguage();
    }

    public ConfluencePage2 getReleasePage() {
        return releasePage;
    }

    public void setReleasePage(ConfluencePage2 releasePage) {
        this.releasePage = releasePage;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public SeiteSO getCustomerPage() {
        return customerPage;
    }

    public void setCustomerPage(SeiteSO customerPage) {
        this.customerPage = customerPage;
    }

    public SeiteSO getSectionPage() {
        return sectionPage;
    }

    public void setSectionPage(SeiteSO releaseSectionPage) {
        this.sectionPage = releaseSectionPage;
    }

    public SeiteSO getResultingReleasePage() {
        return resultingReleasePage;
    }

    public void setResultingReleasePage(SeiteSO resultingReleasePage) {
        this.resultingReleasePage = resultingReleasePage;
    }
    
    public String getPageId() {
        return releaseId;
    }
    
    public void setPageId(String pageId) {
        releaseId = pageId;
    }

    public String getReleaseNumber() {
        return releaseNumber;
    }

    public void setReleaseNumber(String releaseNumber) {
        this.releaseNumber = releaseNumber;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }
}
