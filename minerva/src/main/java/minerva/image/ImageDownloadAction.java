package minerva.image;

import java.io.File;
import java.io.IOException;

import org.pmw.tinylog.Logger;

import com.google.common.io.Files;

import github.soltaufintel.amalia.web.image.AbstractImageDownload;
import github.soltaufintel.amalia.web.image.BinaryData;
import github.soltaufintel.amalia.web.image.IBinaryDataLoader;
import minerva.model.BookSO;
import minerva.model.StatesSO;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;

public class ImageDownloadAction extends AbstractImageDownload {

	@Override
	protected IBinaryDataLoader getLoader() {
		String branch = ctx.pathParam("branch");
		String bookFolder = ctx.pathParam("book");
		String dn = "img/" + ctx.pathParam("id") + "/" + ctx.pathParam("dn");
		if (dn.contains("..") || dn.contains(":")) { // Angreiferschutz
			Logger.error("[ImageDownloadAction] Illegal filename: " + dn);
			throw new RuntimeException("Illegal filename!");
		}
		
		UserSO user = StatesSO.get(ctx).getUser();
		WorkspaceSO workspace = user.getWorkspace(branch);
		BookSO book = workspace.getBooks().byFolder(bookFolder);
		File file = new File(book.getFolder(), dn);
		BinaryData ret = new BinaryData(null, dn) {
			@Override
			public byte[] getData() {
				try {
					return Files.toByteArray(file);
				} catch (IOException e) {
					Logger.error("[ImageDownloadAction] " + e.getClass().getSimpleName() + ": " + e.getMessage());
					throw new RuntimeException("File not found!");
				}
			}
		};
		return () -> ret;
	}
}
