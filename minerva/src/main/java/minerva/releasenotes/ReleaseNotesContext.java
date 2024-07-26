package minerva.releasenotes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class ReleaseNotesContext {
    private final ReleaseNotesConfig config;
    private String pageId;
    private final BookSO book;
    private final Map<String, String> files = new HashMap<>();
    private SeiteSO customerPage;
    private SeiteSO sectionPage;
    private ResultingReleasePage resultingReleasePage;
    private String releaseNumber;
    private String project;
    
    public ReleaseNotesContext(ReleaseNotesConfig config, String pageId, BookSO book) {
        this.config = config;
        this.pageId = pageId;
        this.book = book;
    }

    public ReleaseNotesConfig getConfig() {
        return config;
    }

    public BookSO getBook() {
        return book;
    }

    public String getLang() {
        return config.getLanguage();
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

    public ResultingReleasePage getResultingReleasePage() {
        return resultingReleasePage;
    }

    public void setResultingReleasePage(SeiteSO resultingReleasePage) {
        this.resultingReleasePage = new ResultingReleasePage(resultingReleasePage);
    }
    
    public String getPageId() {
        return pageId;
    }
    
    public void setPageId(String pageId) {
        this.pageId = pageId;
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
    
    public static class ResultingReleasePage {
        private final SeiteSO seite;
        private final Set<String> images = new TreeSet<>();
        
        public ResultingReleasePage(SeiteSO seite) {
            this.seite = seite;
        }

        public SeiteSO getSeite() {
            return seite;
        }
        
        public String getId() {
            return seite.getId();
        }

        public Set<String> getImages() {
            return images;
        }
    }
}
