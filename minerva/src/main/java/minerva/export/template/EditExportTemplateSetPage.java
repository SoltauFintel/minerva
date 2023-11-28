package minerva.export.template;

import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.workspace.WPage;

public class EditExportTemplateSetPage extends WPage {
    private ExportTemplatesService x;
    private Map<String, String> files;
    
    @Override
    protected void execute() {
    	String id = ctx.pathParam("id");
    	
        x = new ExportTemplatesService(workspace);
        List<ExportTemplateSet> all = x.loadAll();
        ExportTemplateSet set = x.load(id);
        
        if (isPOST()) {
        	set.setName(ctx.formParam("etsname"));
			if (StringService.isNullOrEmpty(set.getName())) {
				throw new RuntimeException("Name must not be empty!");
			}
			for (ExportTemplateSet i : all) {
				if (!i.getId().equals(set.getId()) && i.getName().equalsIgnoreCase(set.getName())) {
					throw new RuntimeException("Please enter an unique name!");
				}
			}
        	set.setCustomer(ctx.formParam("customer"));
        	set.setBooks(ctx.formParam("books"));
        	set.setBook(ctx.formParam("book"));
        	set.setPage(ctx.formParam("page"));
        	set.setTemplate(ctx.formParam("template"));
        	set.setStyles(ctx.formParam("templateCss"));
        	set.setPdfStyles(ctx.formParam("pdfCss"));
        	x.save(set);

            Logger.info(user.getLogin() + " | saved export template set: " + set.getName());
            user.log("saved export template set " + set.getName());
            
            ctx.redirect("/ets/" + esc(branch));
        } else {
			header(n("exportTemplates"));
            put("id", esc(set.getId()));
            put("etsname", esc(set.getName()));
            put("customer", esc(set.getCustomer()));
            put("books", esc(set.getBooks()));
            put("book", esc(set.getBook()));
            put("page", esc(set.getPage()));
            put("template", esc(set.getTemplate()));
            put("templateCss", esc(set.getStyles()));
            put("pdfCss", esc(set.getPdfStyles()));
        }
    }
    
    private void save(String dn, String content) {
        files.put(workspace.getFolder() + "/" + dn, content);
    }
}
