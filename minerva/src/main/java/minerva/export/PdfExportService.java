package minerva.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;
import minerva.model.WorkspaceSO;

public class PdfExportService extends MultiPageHtmlExportService {
	private final List<String> errorMessages = new ArrayList<>();
	private final StringBuilder sb = new StringBuilder();
	private final String pdfCss;
	private String imageBaseDir;
	public File pdfFile;
	private String bookTitle;
	
	public PdfExportService(WorkspaceSO workspace, String customer, String language) {
		super(workspace, customer, language);
		exclusionsService.setContext("PDF");
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
		bookTitle = book.getBook().getTitle().getString(lang);
		Logger.info("exporting book \"" + bookTitle + "\"...");
		imageBaseDir = book.getFolder();
		
		File outputFolder = super.saveBook(book);
		
		Logger.info("creating PDF file...");
		pdfFile = new File(outputFolder, outputFolder.getName() + ".pdf");
        PdfWriter pdf = new PdfWriter();
		pdf.writePDF(createFinalHtmlDocument(), true, pdfFile);

		errorMessages.addAll(pdf.getErrorMessages());
		Logger.info("error messages: " + errorMessages.size());
		return outputFolder;
	}
	
	@Override
	public File saveSeite(SeiteSO seite) {
		throw new UnsupportedOperationException("Derzeit kann man nur ein Buch als PDF ausgeben."); // TO-DO
	}
	
	@Override
	public void saveSeitenTo(SeitenSO seiten, SeiteSO parent, Chapter chapter, File outputFolder) {
		super.saveSeitenTo(seiten, parent, chapter, outputFolder);
	}
	
	@Override
	protected void saveSeiteTo(SeiteSO seite, SeiteSO parent, Chapter chapter, File outputFolder) {
		String title = seite.getSeite().getTitle().getString(lang);
	    String html = getHtml(seite, title, outputFolder);
	    if (html == null) {
	    	return;
	    }
	
		sb.append("<div id=\"");
		sb.append(seite.getId());
		sb.append("\" class=\"page\"");
		if (chapter.getLayer() == 1) {
			sb.append(" style=\"page-break-before: always;\"");
		}
	    sb.append(">\n  <h1 class=\"page-title\">");
	    sb.append(chapter.toString());
	    sb.append(" ");
	    sb.append(title.replace("&", "&amp;"));
	    sb.append("</h1>\n");
		sb.append(html);
	    sb.append("\n</div>\n\n");
	}

	private String getHtml(SeiteSO seite, String title, File outputFolder) {
        String html = super.getBody(seite.getContent().getString(lang), title);
        String info = seite.getId() + ": \"" + title + "\"";
        html = HtmlForPdf.processHtml(html, getDoctype(), info, imageBaseDir, errorMessages);
        if (html != null) {
        	html = super.formulas2images(html, seite, outputFolder, title);
        }
        return html;
	}

	private String createFinalHtmlDocument() {
		StringBuilder html = new StringBuilder();
        html.append(getDoctype());
		html.append("<html><head>\n<bookmarks>");
		// PDF TOC
		bookmarks(bookmarks, html);
		html.append("</bookmarks>\n<style>\n");
		html.append(pdfCss);
        html.append("</style>\n</head>\n<body>\n");

        // Cover
        String customer = exclusionsService.getCustomer();
		if (customer.toLowerCase().equals(customer)) {
			customer = customer.toUpperCase();
		}
        String name = "X-map F1";
        if ("DEVKH1".equalsIgnoreCase(customer)) {
            name = "X-map H1";
        }
		html.append("<div class=\"cover\"><h1>");
		html.append(name);
		html.append("<br/>");
		html.append(bookTitle.replace("&", "&amp;"));
		html.append("</h1><h2>");
		html.append(customer);
		html.append("</h2><p class=\"copyright\">Copyright by X-map AG</p></div>\n");
		
        // TOC
		html.append("<div class=\"toc\" style=\"page-break-before: always;\">\n<h1>");
		html.append("de".equals(lang) ? "Inhaltsverzeichnis" : "Table of contents");
		html.append("</h1>");
		if (bookmarks.size() == 1) { // typical for release notes book
			toc(bookmarks.get(0).getBookmarks(), html);
		} else {
			toc(bookmarks, html);
		}
		html.append("</div>\n");

		// Pages
        html.append(sb.toString());
        
        html.append("</body>\n</html>\n");
		return html.toString();
	}
	
	private void toc(List<Bookmark> list, StringBuilder html) {
		for (Bookmark bm : list) {
			html.append("<p><a href=\"#");
			html.append(bm.getId());
			html.append("\">");
			html.append(bm.getTitle().replace("&", "&amp;"));
			html.append("</a></p>");
		}
	}

	private String getDoctype() {
        return "<!DOCTYPE html PUBLIC \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//" // damit &uuml; funktioniert
        		+ lang.toUpperCase() + "\" \"\">\n";
	}
	
	private void bookmarks(List<Bookmark> bookmarks, StringBuilder html) {
		for (Bookmark bm : bookmarks) {
			html.append("<bookmark href=\"#");
			html.append(bm.getId());
			html.append("\" name=\"");
			html.append(bm.getTitle().replace("&", "&amp;"));
			if (bm.getBookmarks().isEmpty()) {
				html.append("\"/>\n");
			} else {
				html.append("\">\n");
				bookmarks(bm.getBookmarks(), html);
				html.append("</bookmark>\n");
			}
		}
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
