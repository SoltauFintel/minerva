package minerva.mask;

import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class FeatureFieldsService {

    public FeatureFields get(SeiteSO seite) {
        FeatureFields ff = new MultiPurposeDirAccess(seite.getBook().dao()).load(dn(seite), FeatureFields.class);
        if (ff == null) {
            ff = new FeatureFields();
            ff.setSeiteId(seite.getId());
            ff.setMaskTag(seite.getFeatureTag());
        }
        return ff;
    }
    
    public void set(SeiteSO seite, FeatureFields featureFields) {
        if (StringService.isNullOrEmpty(featureFields.getSeiteId())
                || StringService.isNullOrEmpty(featureFields.getMaskTag())) {
            throw new IllegalArgumentException("seiteId and/or maskTag must not be empty");
        }
        BookSO book = seite.getBook();
        new MultiPurposeDirAccess(book.dao()).save(dn(seite), featureFields, new CommitMessage(seite, "feature fields"), book.getWorkspace());
    }
    
    public void delete(SeiteSO seite) {
        BookSO book = seite.getBook();
        if (!new MultiPurposeDirAccess(book.dao()).delete(dn(seite), new CommitMessage(seite, "feature fields deleted"),
                book.getWorkspace())) {
            throw new RuntimeException("Error deleting feature fileds for page " + seite.getId());
        }
    }
    
    private String dn(SeiteSO seite) {
        return seite.getBook().getFolder() + "/" + seite.getId() + ".ff";
    }
}
