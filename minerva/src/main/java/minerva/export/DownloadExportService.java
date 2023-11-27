package minerva.export;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import github.soltaufintel.amalia.spark.Context;
import minerva.base.FileService;

public class DownloadExportService {
	private static final Map<String, File> downloads = new HashMap<>();
	
	public String prepareDownload(File sourceFolder, Context ctx) {
		String id;
		File pdfFile = new File(sourceFolder, sourceFolder.getName() + ".pdf");
		if (pdfFile.isFile()) {
			id = register(pdfFile);
			Logger.info(pdfFile.getAbsolutePath() + " => " + id);
		} else {
			File zipFile = new File(sourceFolder.getParentFile(), sourceFolder.getName() + ".zip");
			FileService.zip(sourceFolder, zipFile);
			id = register(zipFile);
			Logger.info(zipFile.getAbsolutePath() + " => " + id);
		}
		return id;
	}

	private String register(File file) {
		String id = IdGenerator.createId6();
		downloads.put(id, file);
		return id;
	}
	
	public String getFilename(String id) {
		File file = downloads.get(id);
		return file == null ? null : file.getName();
	}
	
	public File pop(String id) {
		File ret = downloads.get(id);
		downloads.remove(id);
		return ret;
	}
}
