package minerva.export;

import static gitper.base.StringService.upper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.export.template.ExportTemplateSet;
import minerva.export.template.ExportTemplatesService;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class ExportPage extends WPage {
    private static final String ALL = "_all_";
    private static final String PAGE = "_page_";
    private static final String PAGE_WITH_FEATURES = "_pagef_";
    private String lang;
    
    @Override
    protected void execute() {
        int nBooks = workspace.getBooks().size();
        if (nBooks == 0) {
            throw new RuntimeException("There are no books!");
        }
        lang = user.getGuiLanguage();

        if (isPOST()) {
            callExportDownload();
        } else {
            String seiteId = ctx.queryParam("seite");
            Logger.info("ExportPage (Auswahl was exportiert werden soll): " + seiteId);
            
            header(n("export"));
            List<IdAndLabel> items = getItems(workspace);
            List<String> customers = new ArrayList<>(workspace.getExclusions().getCustomers());
            customers.add(0, "-");
            
            ExportUserSettings us = user.getUser().getExport();
            if (us == null) {
               us = new ExportUserSettings();
               us.setItem(items.get(nBooks > 1 ? 1 : 0).getId());
               us.setCustomer(customers.get(0));
               us.setLang(langs.get(0).toUpperCase());
            }
            put("seite", "");
            if (!StringService.isNullOrEmpty(seiteId)) {
                SeiteSO seite = workspace.findPage(seiteId);
                if (seite != null) {
                    put("seite", esc(seiteId));
                    us.setItem(seite.isFeatureTree() ? PAGE_WITH_FEATURES : PAGE);
                }
            }
            
            combobox_idAndLabel("items", items, us.getItem(), false);
            combobox("customers", upper(customers), us.getCustomer(), false);
            combobox("langs", upper(langs), us.getLang(), false);
            
            List<String> formats = new ArrayList<>();
            formats.add(n("multiPageHtml"));
            formats.add("PDF");
            combobox("formats", formats, us.getFormat(), false);
            
            List<String> exportTemplateSetNames = new ExportTemplatesService(workspace).loadAll()
                    .stream().map(i -> i.getName()).collect(Collectors.toList());
            if (exportTemplateSetNames.isEmpty()) {
                throw new UserMessage("no-export-template-sets", user);
            }
            combobox("templates", exportTemplateSetNames, us.getTemplate(), false);
            put("withCover", us.isCover());
            put("withTOC", us.isToc());
            put("withChapters", us.isChapters());
            put("templatesAllowed", UserAccess.hasExportRight(user.getLogin()));
        }
    }

    private List<IdAndLabel> getItems(WorkspaceSO workspace) {
        List<IdAndLabel> items = new ArrayList<>();
        if (workspace.getBooks().size() > 1) {
            items.add(new ExportItem(ALL, n("allBooks")));
        }
        workspace.getBooks().forEach(book -> items.add(new ExportItem(book.getBook().getFolder(), book.getBook().getTitle().getString(lang))));
        items.add(new ExportItem(PAGE, n("selectPage")));
        items.add(new ExportItem(PAGE_WITH_FEATURES, n("selectPageWithFeatures")));
        return items;
    }

    private void callExportDownload() {
        String item = ctx.formParam("item");
        String customer = ctx.formParam("customer");
        String lang = ctx.formParam("lang");
        String format = ctx.formParam("format");
        String template = ctx.formParam("template");
        boolean withCover = "on".equals(ctx.formParam("withCover"));
        boolean withTOC = "on".equals(ctx.formParam("withTOC"));
        boolean withChapters = "on".equals(ctx.formParam("withChapters"));
        String seite = ctx.formParam("seite");

        user.saveExportSettings(item, customer, lang, format, template, withCover, withTOC, withChapters);
        
        String q = "/export?lang=" + u(lang.toLowerCase())
                    + "&customer=" + u(customer.toLowerCase())
                    + "&template=" + u(templateName2Id(template))
                    + "&o=" + (withCover ? "c" : "") + (withTOC ? "i" : "") + (withChapters ? "k" : "");
        if ("PDF".equals(format)) {
            q += "&w=pdf";
        }
        if (!StringService.isNullOrEmpty(seite)) {
            q += "&seite=" + u(seite);
        }

        String link;
        if (ALL.equals(item)) { // all books -> ExportWorkspaceAction
        	link = "/w/" + branch + "/books" + q;
        } else if (PAGE_WITH_FEATURES.equals(item)) { // -> SeitenauswahlPage
            link = "/w/" + branch + "/pages" + q + "&f=1";
        } else if (PAGE.equals(item)) { // -> SeitenauswahlPage
        	link = "/w/" + branch + "/pages" + q;
        } else { // book -> ExportBookAction
        	link = "/b/" + branch + "/" + esc(item) + q;
        }
        Logger.info("-> " + link);
        ctx.redirect(link);        
    }

    private String templateName2Id(String name) {
        for (ExportTemplateSet template : new ExportTemplatesService(workspace).loadAll()) {
            if (template.getName().equals(name)) {
                return template.getId();
            }
        }
        throw new RuntimeException("Template with name \"" + name + "\" does not exist!");
    }
}
