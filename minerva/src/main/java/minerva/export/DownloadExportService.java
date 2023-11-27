package minerva.export;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.base.FileService;
import minerva.base.NLS;

public class DownloadExportService {
	private static final Map<String, File> downloads = new HashMap<>();
	
	public String prepareDownload(File sourceFolder) {
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

	public String prepareDownload(List<File> pdfFiles, String lang) {
		String id;
		if (pdfFiles.size() == 1) {
			id = register(pdfFiles.get(0));
			Logger.info(pdfFiles.get(0).getAbsolutePath() + " => " + id);
		} else {
			File zipFile = new File(pdfFiles.get(0).getParentFile().getParentFile(), NLS.get(lang, "allBooks") + ".zip");
			FileService.zip(pdfFiles, zipFile);
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
