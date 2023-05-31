package minerva.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.web.action.JsonAction;
import minerva.base.UserMessage;
import minerva.image.ImageUploadAction.Success;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import spark.utils.IOUtils;

public class ImageUploadAction extends JsonAction<Success> {

	@Override
	protected void execute() {
		String branch = ctx.pathParam("branch");
		String bookFolder = ctx.pathParam("book");
		String id = ctx.pathParam("id");

		UserSO user = StatesSO.get(ctx).getUser();
		WorkspaceSO workspace = user.getWorkspace(branch);
		BookSO book = workspace.getBooks().byFolder(bookFolder);
		SeiteSO seite = book.getSeiten().byId(id);

		ctx.req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("upload"));
		ctx.res.type("application/json");
		Part part;
		try {
			part = ctx.req.raw().getPart("upload");
		} catch (IOException | ServletException e) {
			Logger.error(e);
			throw new RuntimeException("Error uploading image!");
		}
		String submittedFilename = part.getSubmittedFileName();
		if (part.getSize() > 1024l * 1024 * 1024 * 10) { // 10 MB
			throw new UserMessage("error.imageTooBig", workspace);
		}

		String filename = "img/" + seite.getId() + "/" + submittedFilename;
		File file = new File(book.getFolder(), filename);
		if (file.isFile()) { // Name schon vergeben
			int o = filename.lastIndexOf(".");
			if (o >= 0) {
				filename = filename.substring(0, o) + "-" + IdGenerator.createId6() + filename.substring(o);
			} else {
				filename += "-" + IdGenerator.createId6();
			}
			Logger.info("[ImageUploadAction] Dateiname schon vergeben. Geändert auf " + filename);
			file = new File(book.getFolder(), filename);
			if (file.isFile()) { // Das sollte doch niemals passieren.
				Logger.error("File already exists: " + file.getAbsolutePath());
				throw new RuntimeException("Error uploading image! Try another filename.");
			}
		}
		file.getParentFile().mkdirs();
		try (FileOutputStream fos = new FileOutputStream(file)) {
			IOUtils.copy(part.getInputStream(), fos);
		} catch (IOException e) {
			Logger.error(e);
			throw new RuntimeException("Error uploading image!");
		}
		
		seite.getImages().add(filename);

		// Response must be JSON, containing url field.
		Success ret = new Success();
		ret.setUrl(filename); // relative filename (e.g. branch name cannot be saved!)
		result = ret;
	}

	public static class Success {
		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}
}
