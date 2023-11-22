package minerva.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.pmw.tinylog.Logger;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.Diagnostic;
import com.openhtmltopdf.util.XRLog;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

public class PdfExportService extends MultiPageHtmlExportService {
	private final String pdfCss;
	public File pdfFile;
	public static boolean check = false;
	private StringBuilder sb = new StringBuilder();
	private boolean firstPage = true;
	private String imageBaseDir;
	private List<String> errorMessages = new ArrayList<>();
	
	// TODO Klasse zerschlagen? PDF-Technik, HTML besorgen+aufbereiten, Seitensteuerung, CSS
	
	public PdfExportService(WorkspaceSO workspace, String customer, String language) {
		super(workspace, customer, language);
		pdfCss = new ExportTemplatesService(workspace).loadTemplate(ExportTemplatesService.PDF_CSS);
		if (StringService.isNullOrEmpty(pdfCss)) {
			Logger.warn("PDF CSS is empty!");
		}
	}
	
	@Override
	public File saveBook(BookSO book) {
		Logger.info("exporting book \"" + book.getBook().getTitle().getString(lang) + "\"...");
		imageBaseDir = book.getFolder();
		File outputFolder = super.saveBook(book);
		Logger.info("creating PDF file...");
        StringBuilder s = createHtmlContent();
		pdfFile = new File(outputFolder, book.getTitle() + ".pdf");
		writePDF(s);
		return outputFolder;
	}
	
	private StringBuilder createHtmlContent() {
		StringBuilder html = new StringBuilder();
        html.append(getDoctype());
        html.append("<html><head><style>\n");
        html.append(pdfCss);
        html.append("</style></head><body>\n");
        html.append(sb.toString());
        html.append("</body></html>\n");
		return html;
	}

	private String getDoctype() {
        return "<!DOCTYPE html PUBLIC \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//" // damit &uuml; funktioniert
        		+ lang.toUpperCase() + "\" \"\">\n";
	}

	private void writePDF(StringBuilder s) {
		//FileService.savePlainTextFile(new File("pdf-export.html"), s.toString()); // für die Fehleranalyse
		try (OutputStream os = new FileOutputStream(pdfFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();

            XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, java.util.logging.Level.OFF)); // no output to syserr
    		List<Diagnostic> diagonstics = new ArrayList<>();
    		builder.withDiagnosticConsumer(diagonstics::add); // https://github.com/danfickle/openhtmltopdf/wiki/Logging
            
            builder.useFont(new File("fonts/NotoSans-Regular.ttf"), "Noto Sans");
            builder.withHtmlContent(s.toString(), "/");
            builder.toStream(os);
            builder.run();
			
            warnings(diagonstics);
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
	
	@Override
	protected void saveSeiteTo(SeiteSO seite, SeiteSO parent, File outputFolder) {
		String title = seite.getSeite().getTitle().getString(lang);
        String html = getHTML(seite, title, outputFolder);
        if (html == null) {
        	return;
        }

        if (firstPage) {
        	sb.append("<div id=\"" + seite.getId() + "\" class=\"page\">");
        	firstPage = false;
        } else {
        	sb.append("<div id=\"" + seite.getId() + "\" class=\"page\" style=\"page-break-before: always;\">");
        }
        sb.append("\n<h1 class=\"page-title\">");
        sb.append(title.replace("&", "&amp;"));
        sb.append("</h1>\n");
		sb.append(html);
        sb.append("</div>\n\n");
	}
	
	private String getHTML(SeiteSO seite, String title, File outputFolder) {
		String info = seite.getId() + ": \"" + title + "\"";
        String html = getBody(seite.getContent().getString(lang), title);
        html = toXHTML(html);
        if (!checkHtml(html, info)) {
        	return null;
        }
        html = modifyLinks(html, info);
		html = images(html, info);
		return formulas2images(html, seite, outputFolder, title);
	}

	private String toXHTML(String html) {
		html = html.replace("<col>\n", "<col/>\n");
		html = closeTag(html, "<img");
		html = closeTag(html, "<br");
		html = closeTag(html, "<col ");
		return html;
	}

	private String closeTag(String html, String tag) {
		int o = html.indexOf(tag);
		while (o >= 0) {
			int oo = html.indexOf(">", o);
			if (oo > o) {
				html = html.substring(0, oo) + "/" + html.substring(oo);
			}

			o = html.indexOf(tag, o + tag.length() + 1);
		}
		return html;
	}

	/**
	 * Die einzelnen Seiten separat prüfen, um herauszufinden wo es noch broken HTML gibt.
	 * Das HTML muss dann in toXHTML() korrigiert werden.
	 */
	private boolean checkHtml(String html, String info) {
		if (!check) {
			return true;
		}
		try {
			Path file = Files.createTempFile("Minerva-PDF-", ".pdf");
			try (OutputStream os = new FileOutputStream(file.toFile())) {
				PdfRendererBuilder builder = new PdfRendererBuilder();
				builder.withHtmlContent(getDoctype() + html, "/");
				builder.toStream(os);
				builder.run();
				return true;
			}
		} catch (Exception e) {
			Logger.error(info + " will be excluded from PDF export because of error: " + e.getMessage());
			return false;
		}
	}
    
    private String modifyLinks(String html, String info) { // similar to super.addDotHtml()
        List<Link> links = LinkService.extractLinks(html, false);
        for (Link link : links) {
            String href = link.getHref();
            if (!href.startsWith("http://") && !href.startsWith("https://")) {
                int o = href.lastIndexOf("#");
                if (o >= 0) {
                	errorMessages.add(info + ": anchor not supported. Just link to page. link: " + href);
                    href = href.substring(0, o);
                }
                html = html.replace("<a href=\"" + href + "\">", "<a href=\"#" + href + "\">");
            }
        }
        return html;
    }

	/**
	 * Replace img src to absolute paths.
	 */
    private String images(String html, String info) {
		int o = html.indexOf("<img");
		while (o >= 0) {
			int diff = 0;
			int o1 = html.indexOf("src=\"", o);
			if (o1 > o) {
				o1 += "src=\"".length();
				int o2 = html.indexOf("\"", o1);
				if (o2 > o1) {
					String src = html.substring(o1, o2);
					if (!src.startsWith("http")) {
						File f = new File(imageBaseDir, src);
						String newSrc = "file:///" + f.getAbsolutePath().replace("\\", "/");
						html = html.substring(0, o1) + newSrc + html.substring(o2);
						diff = newSrc.length() - src.length();
					} else {
						errorMessages.add(info +" has an image with http URL: " + src);
					}
				}
			}

			o = html.indexOf("<img", o + "<img".length() + diff);
		}
		return html;
    }

	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
