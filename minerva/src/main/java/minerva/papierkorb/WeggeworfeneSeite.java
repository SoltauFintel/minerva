package minerva.papierkorb;

import minerva.base.NlsString;

/**
 * Discarded page as object in trash bin. Saved as data/papierkorb/{id}/papierkorb.json.
 */
public class WeggeworfeneSeite extends WSeite {
    private String bookFolder;
    private String parentId;
    private final NlsString parentTitle = new NlsString();
    private String deleteDate;
    private String deletedBy;

    public String getBookFolder() {
        return bookFolder;
    }

    public void setBookFolder(String bookFolder) {
        this.bookFolder = bookFolder;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDeleteDate() {
        return deleteDate;
    }

    public void setDeleteDate(String deleteDate) {
        this.deleteDate = deleteDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public NlsString getParentTitle() {
        return parentTitle;
    }
}
