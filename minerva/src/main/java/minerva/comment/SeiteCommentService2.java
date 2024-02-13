package minerva.comment;

import minerva.access.SimpleDirAccess;
import minerva.model.SeiteSO;

/**
 * Seite-specific comments service
 */
public class SeiteCommentService2 extends AbstractCommentService2 {
    
    public SeiteCommentService2(SeiteSO seite) {
        super(new SimpleDirAccess(seite), SeiteCommentService.calcDir(seite));
    }
}
