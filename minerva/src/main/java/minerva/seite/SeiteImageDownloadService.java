package minerva.seite;

import java.io.File;

import github.soltaufintel.amalia.spark.Context;
import minerva.image.ImageDownloadService;
import minerva.model.BookSO;
import minerva.model.StatesSO;

public class SeiteImageDownloadService extends ImageDownloadService {

    public SeiteImageDownloadService(Context ctx) {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");
        String dn = dn(ctx);
        
        BookSO book = StatesSO.get(ctx).getUser().getWorkspace(branch).getBooks().byFolder(bookFolder);
        file = new File(book.getFolder(), "img/" + id + "/" + dn);
    }
}
