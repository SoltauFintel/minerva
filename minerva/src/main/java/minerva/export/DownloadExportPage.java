package minerva.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.base.UserMessage;
import minerva.workspace.WPage;

public class DownloadExportPage extends WPage {

	@Override
	protected void execute() {
		String id = ctx.pathParam("id");
		String mode = ctx.queryParam("mode");
		
		if ("dl".equals(mode)) {
			render = false;
			File file = GenericExportService.pop(id);
			if (file != null && file.isFile()) {
				if (file.getName().endsWith(".pdf")) {
					ctx.res.type("application/pdf");
					download(file);
				} else if (file.getName().endsWith(".zip")) {
					ctx.res.type("application/zip");
					download(file);
				}
			} else {
				throw new UserMessage("export-already-downloaded", user);
			}
		} else {
			String dn = GenericExportService.getFilename(id);
			if (dn == null) {
				throw new UserMessage("export-already-downloaded", user);
			}
			put("id", esc(id));
			put("dn", esc(dn));
		}
	}
	
	private void download(File file) {
		ctx.res.header("Content-Disposition", "inline; filename=\"" + file.getName() + "\""); // inline -> opens PDF in new tab instead of showing it in the browser download list
		try {
			ctx.res.raw().getOutputStream().write(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Logger.info("delete folder: " + file.getParentFile().getAbsolutePath());
		FileService.deleteFolder(file.getParentFile());
	}
}
