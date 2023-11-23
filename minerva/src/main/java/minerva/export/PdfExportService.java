package minerva.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class PdfExportService extends MultiPageHtmlExportService {
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
	protected void saveSeiteTo(SeiteSO seite, SeiteSO parent, File outputFolder) {
		String title = seite.getSeite().getTitle().getString(lang);
	    String html = getHtml(seite, title, outputFolder);
	    if (html == null) {
	    	return;
	    }
	
		sb.append("<div id=\"");
		sb.append(seite.getId());
		sb.append("\" class=\"page\"");
		if (firstPage) {
			firstPage = false;
		} else {
			sb.append(" style=\"page-break-before: always;\"");
		}
	    sb.append(">\n<h1 class=\"page-title\">");
	    sb.append(title.replace("&", "&amp;"));
	    sb.append("</h1>\n");
		sb.append(html);
	    sb.append("</div>\n\n");
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
