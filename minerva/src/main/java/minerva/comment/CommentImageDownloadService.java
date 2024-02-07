package minerva.comment;

import java.io.File;

import github.soltaufintel.amalia.spark.Context;
import minerva.image.ImageDownloadService;
import minerva.model.BookSO;
import minerva.model.StatesSO;

public class CommentImageDownloadService extends ImageDownloadService {

    public CommentImageDownloadService(Context ctx) {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String seiteId = ctx.pathParam("id");
        String commentId = ctx.pathParam("commentId");
        String dn = dn(ctx);
        
        BookSO book = StatesSO.get(ctx).getUser().getWorkspace(branch).getBooks().byFolder(bookFolder);
        file = new File(book.getFolder(), "comments/" + seiteId + "/img/" + commentId + "/" + dn);
    }
}
