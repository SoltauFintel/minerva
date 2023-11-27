package minerva.export.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.pmw.tinylog.Logger;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.Diagnostic;
import com.openhtmltopdf.util.XRLog;

import minerva.base.FileService;

public class PdfWriter {
	private final List<String> errorMessages = new ArrayList<>();

	public void writePDF(String html, boolean withDiagnostics, File file) {
		try (OutputStream os = new FileOutputStream(file); InputStream notoSans = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf")) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            List<Diagnostic> diagonstics = null;
            if (withDiagnostics) {
	            if (org.pmw.tinylog.Level.DEBUG.equals(Logger.getLevel())) {
	            	File d = Files.createTempFile("pdf-export", ".html").toFile();
					FileService.savePlainTextFile(d, html);
					Logger.debug("für die Fehleranalyse: " + d.getAbsolutePath());
	            } else {
	            	XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF)); // no output to syserr
	            }
	    		diagonstics = new ArrayList<>();
	    		builder.withDiagnosticConsumer(diagonstics::add); // https://github.com/danfickle/openhtmltopdf/wiki/Logging
            }

			builder.useFont(() -> notoSans, "Noto Sans");
    		builder.withHtmlContent(html, "/");
            builder.toStream(os).run();

            if (withDiagnostics) {
            	warnings(diagonstics);
            }
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void warnings(List<Diagnostic> diagnostics) {
		int eColor = 0;
		int eImgJira = 0;
		int eImgOldMinervaUrl = 0;
		for (Diagnostic d : diagnostics) {
			if (d.getLevel().equals(Level.WARNING) || d.getLevel().equals(Level.SEVERE)) {
				String m = d.getFormattedMessage();
				if (m.contains("color must be")) {
					eColor++;
				} else if (m.contains("Unrecognized image format for: http://jira01")) {
					eImgJira++;
					// Da muss die Minerva Seite korrigiert werden.
				} else if (m.contains("Can't read image file; unexpected problem for URI 'http://jira01")) { // ignore, same as eImgJira
				} else if (m.contains("IO problem for http://docker01:9000")) {
					eImgOldMinervaUrl++;
					// Da muss die Minerva Seite korrigiert werden.
				} else { // Die sonstigen Meldungen könnten interessant sein.
					errorMessages.add(d.getFormattedMessage());
				}
			}
		}
		errorMessages.add("INFO: color warnings: " + eColor + ", Confluence image warnings: " + eImgJira
				+ ", old Minerva image warnings: " + eImgOldMinervaUrl);
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
