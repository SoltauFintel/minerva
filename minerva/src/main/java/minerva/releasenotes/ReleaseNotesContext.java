package minerva.releasenotes;

import java.util.HashMap;
import java.util.Map;

import minerva.confluence.ConfluencePage2;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class ReleaseNotesContext {
    private final String spaceKey;
    private final String rootTitle;
    private final String releaseTitle;
    private final BookSO book;
    private final String lang;
    private final Map<String, String> files = new HashMap<>();
    private ConfluencePage2 releasePage;
    private SeiteSO customerPage;
    private SeiteSO sectionPage;
    private SeiteSO resultingReleasePage;
    
    public ReleaseNotesContext(String spaceKey, String rootTitle, String releaseTitle,
            BookSO book, String lang) {
        this.spaceKey = spaceKey;
        this.rootTitle = rootTitle;
        this.releaseTitle = releaseTitle;
        this.book = book;
        this.lang = lang;
    }

    public String getSpaceKey() {
        return spaceKey;
    }

    public String getRootTitle() {
        return rootTitle;
    }

    public String getReleaseTitle() {
        return releaseTitle;
    }

    public BookSO getBook() {
        return book;
    }

    public String getLang() {
        return lang;
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
}
