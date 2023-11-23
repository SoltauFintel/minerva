package minerva.export;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.seite.link.Link;
import minerva.seite.link.LinkService;

public class PdfExportService extends MultiPageHtmlExportService {
	public static boolean check = false;
	private final List<String> errorMessages = new ArrayList<>();
	private final StringBuilder sb = new StringBuilder();
	private final String pdfCss;
	private boolean firstPage = true;
	private String imageBaseDir;
	public File pdfFile;
	
	public PdfExportService(WorkspaceSO workspace, String customer, String language) {
		super(workspace, customer, language);
		pdfCss = new ExportTemplatesService(workspace).loadTemplate(ExportTemplatesService.PDF_CSS);
		if (StringService.isNullOrEmpty(pdfCss)) {
			Logger.warn("PDF CSS is empty!");
		}
	}
	
	@Override
	public File saveWorkspace(WorkspaceSO workspace) {
		throw new UnsupportedOperationException("Derzeit kann man nur ein Buch als PDF ausgeben."); // TO-DO
	}
	
	@Override
	public File saveBook(BookSO book) {
		Logger.info("exporting book \"" + book.getBook().getTitle().getString(lang) + "\"...");
		imageBaseDir = book.getFolder();
		
		File outputFolder = super.saveBook(book);
		
		Logger.info("creating PDF file...");
		pdfFile = new File(outputFolder, outputFolder.getName() + ".pdf");
        PdfWriter pdf = new PdfWriter();
		pdf.writePDF(createHtmlContent(), true, pdfFile);

		errorMessages.addAll(pdf.getErrorMessages());
		Logger.info("error messages: " + errorMessages.size());
		return outputFolder;
	}
	
	@Override
	public File saveSeite(SeiteSO seite) {
		throw new UnsupportedOperationException("Derzeit kann man nur ein Buch als PDF ausgeben."); // TO-DO
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
        String html = super.getBody(seite.getContent().getString(lang), title);
        html = toXHTML(html);
        if (!checkHtml(html, info)) {
        	return null;
        }
        html = modifyLinks(html, info);
		html = images(html, info);
		return super.formulas2images(html, seite, outputFolder, title);
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
			File file = Files.createTempFile("Minerva-PDF-", ".pdf").toFile();
			new PdfWriter().writePDF(getDoctype() + html, false, file);
			return true;
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

	private String createHtmlContent() {
		StringBuilder html = new StringBuilder();
        html.append(getDoctype());
        html.append("<html><head><style>\n");
        html.append(pdfCss);
        html.append("</style></head><body>\n");
        html.append(sb.toString());
        html.append("</body></html>\n");
		return html.toString();
	}

	private String getDoctype() {
        return "<!DOCTYPE html PUBLIC \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//" // damit &uuml; funktioniert
        		+ lang.toUpperCase() + "\" \"\">\n";
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
