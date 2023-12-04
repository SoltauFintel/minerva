package minerva.export.pdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.template72.Template;
import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.FileService;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.export.Formula2Image.TransformPath;
import minerva.export.MultiPageHtmlExportService;
import minerva.export.template.ExportTemplatesService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class PdfExportService extends MultiPageHtmlExportService {
	private final List<File> pdfFiles = new ArrayList<>();
	private final List<String> errorMessages = new ArrayList<>();
	private final String pdfCss;
	private StringBuilder sb = new StringBuilder();
	private String imageBaseDir;
	public File pdfFile;
	private String bookTitle;
	
	public PdfExportService(WorkspaceSO workspace, String customer, String language, String templateId) {
		super(workspace, customer, language, templateId);
		exclusionsService.setContext("PDF");
		pdfCss = new ExportTemplatesService(workspace).load(templateId).getPdfStyles();
		if (StringService.isNullOrEmpty(pdfCss)) {
			Logger.warn("PDF CSS is empty!");
		}
	}

	@Override
	protected void init(File outputFolder) {
		outputFolder.mkdirs();
	}
	
	@Override
	public String getBooksExportDownloadId(WorkspaceSO workspace) {
		String id;
    	saveWorkspace(workspace);
		Logger.info("error messages: " + errorMessages.size());
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

	// important for books export
	@Override
	protected void saveBookTo(BookSO book, File outputFolder) {
		if (booksMode) {
			// clear
			sb = new StringBuilder();
			cb = new Bookmark("root", "book");
			bookmarks = cb.getBookmarks();

			prepare(book);
		}

		super.saveBookTo(book, outputFolder);
		
		if (booksMode) {
			createPDF(outputFolder, true);
			pdfFiles.add(pdfFile);
		}
	}
	
	@Override
	public File saveBook(BookSO book) {
		prepare(book);
		File outputFolder = super.saveBook(book);
		createPDF(outputFolder, true);
		Logger.info("error messages: " + errorMessages.size());
		return outputFolder;
	}

	private void prepare(BookSO book) {
		bookTitle = book.getBook().getTitle().getString(lang);
		Logger.info("exporting book \"" + bookTitle + "\"...");
		imageBaseDir = book.getFolder();
	}

	private void createPDF(File outputFolder, boolean withCoverAndToc) {
		Logger.info("creating PDF file...");
		pdfFile = new File(outputFolder, outputFolder.getName() + ".pdf");
        PdfWriter pdf = new PdfWriter();
		pdf.writePDF(createFinalHtmlDocument(withCoverAndToc), true, pdfFile);
		errorMessages.addAll(pdf.getErrorMessages());
	}
	
	@Override
	public File saveSeiten(List<SeiteSO> seiten) {
		prepare(seiten.get(0).getBook()); // TODO es könnten unterschiedliche Bücher sein!
		// TODO Logger.info("just exporting page \"" + seite.getSeite().getTitle().getString(lang) + "\" of that book...");
		
		File outputFolder = super.saveSeiten(seiten);
		
		createPDF(outputFolder, false);
		Logger.info("error messages: " + errorMessages.size());
		return outputFolder;
	}
	
	@Override
	protected void saveIndex(File outputFolder, String dn, DataMap model) { //
	}

	@Override
	protected void copyPageFileAsIndexFile(SeiteSO seite, File outputFolder) { //
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

	private String createFinalHtmlDocument(boolean withCoverAndToc) {
		StringBuilder html = new StringBuilder();
        html.append(getDoctype());
		html.append("<html><head>\n");
		if (withCoverAndToc) {
			// PDF TOC
			html.append("<bookmarks>");
			bookmarks(bookmarks, html);
			html.append("</bookmarks>\n");
		}
		html.append("<style>\n");
		html.append(pdfCss);
        html.append("</style>\n</head>\n<body>\n");

		if (withCoverAndToc) {
	        createCoverAndToc(html);
		}

		// Pages
        html.append(sb.toString());
        
        html.append("</body>\n</html>\n");
		return html.toString();
	}

	private void createCoverAndToc(StringBuilder html) {
		DataMap model = new DataMap();
		
		String customer = exclusionsService.getCustomer();
		if ("-".equals(customer)) {
			customer = "";
		} else if (customer.toLowerCase().equals(customer)) {
			customer = customer.toUpperCase();
		}
		model.put("customer", customer);
		
		model.put("bookTitle", bookTitle.replace("&", "&amp;"));
		model.put("de", "de".equals(lang));
		model.put("cover", true); // TODO
		model.put("toc", true); // TODO

		if (bookmarks.size() == 1 && !bookmarks.get(0).getBookmarks().isEmpty()) { // typical for release notes book
			createTocLines(bookmarks.get(0).getBookmarks(), model);
		} else {
			createTocLines(bookmarks, model);
		}
		
		String aTemplate = exportTemplateSet.getPdfToc();
		html.append(Template.createFromString(aTemplate).withData(model).render());
	}

	private void createTocLines(List<Bookmark> bookmarks, DataMap model) {
		DataList list = model.list("tocLines");
		for (Bookmark b : bookmarks) {
			DataMap map = list.add();
			map.put("id", b.getId());
			map.put("title", b.getTitle().replace("&", "&amp;"));
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
			html.append(bm.getTitle().replace("&", "&amp;").replace("\"", "&quot;"));
			if (bm.getBookmarks().isEmpty()) {
				html.append("\"/>\n");
			} else {
				html.append("\">\n");
				bookmarks(bm.getBookmarks(), html);
				html.append("</bookmark>\n");
			}
		}
	}
	
	@Override
	protected TransformPath getTransformPath() {
		return (path, file) -> "file:///" + file.getAbsolutePath().replace("\\", "/");
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}
}
