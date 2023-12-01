package minerva.export;

import static minerva.base.StringService.umlaute;
import static minerva.base.StringService.upper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.base.UserMessage;
import minerva.export.template.ExportTemplateSet;
import minerva.export.template.ExportTemplatesService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.workspace.WPage;

public class ExportPage extends WPage {
    private static final String W = "###";
    private static final String B = "##";
    private static final String S = "#";
    private String lang;
    
    @Override
    protected void execute() {
//        user.onlyWithExportRight();
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        lang = user.getGuiLanguage();

        if (isPOST()) {
            callExportDownload();
        } else {
            header(n("export"));
            List<String> items = getItems(workspace);
            List<String> customers = new ArrayList<>(workspace.getExclusions().getCustomers());
            customers.add(0, "-");
            
            ExportUserSettings us = user.getUser().getExport();
            if (us == null) {
               us = new ExportUserSettings();
               us.setItem(items.get(1));
               us.setCustomer("-");
               us.setLang(langs.get(0).toUpperCase());
            }
            
            combobox("items", items, us.getItem(), false, model);
            combobox("customers", upper(customers), us.getCustomer(), false, model);
            combobox("langs", upper(langs), us.getLang(), false, model);
            
            List<String> formats = new ArrayList<>();
            formats.add(n("multiPageHtml"));
            formats.add("PDF");
            combobox("formats", formats, us.getFormat(), false, model);
            
            List<String> exportTemplateSetNames = new ExportTemplatesService(workspace).loadAll()
            		.stream().map(i -> i.getName()).collect(Collectors.toList());
            if (exportTemplateSetNames.isEmpty()) {
            	throw new UserMessage("no-export-template-sets", user);
            }
			combobox("templates", exportTemplateSetNames, us.getTemplate(), false, model);
        }
    }
    
    private List<String> getItems(WorkspaceSO workspace) {
        List<String> items = new ArrayList<>();
        List<String> pageTitles = new ArrayList<>();
        items.add(n("allBooks") + " " + W);
        for (BookSO book : workspace.getBooks()) {
            items.add(book.getBook().getTitle().getString(lang) + " " + B + book.getBook().getFolder());
            addPageTitles(book, pageTitles);
        }
        pageTitles.sort((a, b) -> umlaute(a).compareTo(umlaute(b)));
        items.addAll(pageTitles);
        return items;
    }

    private void addPageTitles(BookSO book, List<String> items) {
        for (SeiteSO seite : book.getAlleSeiten()) {
            items.add(seite.getSeite().getTitle().getString(lang) + " " + S + seite.getId());
        }
    }
    
    @Override
    protected String getPage() {
        return super.getPage();
    }

    private void callExportDownload() {
        String item = ctx.formParam("item");
        String customer = ctx.formParam("customer");
        String lang = ctx.formParam("lang");
        String format = ctx.formParam("format");
        String template = ctx.formParam("template");
        user.saveExportSettings(item, customer, lang, format, template);
        
        String q = "/export?lang=" + u(lang.toLowerCase())
        			+ "&customer=" + u(customer.toLowerCase())
        			+ "&template=" + u(templateName2Id(template));
        if ("PDF".equals(format)) {
        	q += "&w=pdf";
        }

        if (item.contains(W)) { // all books
            ctx.redirect("/w/" + branch + "/books" + q);
            
        } else if (item.contains(B)) { // book
            String bookFolder = item.substring(item.lastIndexOf(B) + B.length());
            ctx.redirect("/b/" + branch + "/" + esc(bookFolder) + q);
            
        } else if (item.contains(S)) { // Seite
            String seiteId = item.substring(item.lastIndexOf(S) + S.length());
            SeiteSO seite = getSeite(seiteId);
            if (seite != null) {
                String bookFolder = seite.getBook().getBook().getFolder();
                ctx.redirect("/s/" + branch + "/" + esc(bookFolder) + "/" + seiteId + q);
            } else {
                Logger.error("Page not found for export item: " + item);
                throw new RuntimeException("Page not found");
            }
            
        } else {
            Logger.error("Unknown export item: " + item);
            throw new RuntimeException("Unknown item");
        }
    }

    private String templateName2Id(String name) {
    	for (ExportTemplateSet template : new ExportTemplatesService(workspace).loadAll()) {
			if (template.getName().equals(name)) {
				return template.getId();
			}
		}
    	throw new RuntimeException("Template with name \"" + name + "\" does not exist!");
	}

	private SeiteSO getSeite(String seiteId) {
        for (BookSO book : workspace.getBooks()) {
            SeiteSO seite = book._seiteById(seiteId);
            if (seite != null) {
                return seite;
            }
        }
        return null;
    }
}
