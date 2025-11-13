package github.soltaufintel.amalia.ckeditor;

import github.soltaufintel.amalia.ckeditor.image.ImageDownloadAction;
import github.soltaufintel.amalia.ckeditor.image.ImageUploadAction;
import github.soltaufintel.amalia.web.route.RouteDefinitions;

public class CKEditorRoutes extends RouteDefinitions {

	@Override
	public void routes() {
		get("/image-upload/*", ImageUploadAction.class);
		get("/*/img/:id/:filename", ImageDownloadAction.class);
	}
}
