package minerva.seite;

import github.soltaufintel.amalia.spark.Context;
import minerva.image.ImageUploadService;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;

public class SeiteImageUploadService extends ImageUploadService {
    protected final SeiteSO seite;
    
    public SeiteImageUploadService(Context ctx) {
        String branch = ctx.pathParam("branch");
        String bookFolder = ctx.pathParam("book");
        String id = ctx.pathParam("id");
        workspace = StatesSO.get(ctx).getUser().getWorkspace(branch);
        seite = workspace.getBooks().byFolder(bookFolder).seiteById(id);
    }

    @Override
    protected String getFolder() {
        return seite.getBook().getFolder();
    }
    
    @Override
    public void setSubmittedFileName(String submittedFilename) {
        filename = "img/" + seite.getId() + "/" + submittedFilename;
    }
    
    @Override
    public void success() {
        seite.getImages().add(filename);
    }
}
