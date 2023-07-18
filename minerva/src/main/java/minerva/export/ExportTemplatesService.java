package minerva.export;

import java.io.File;

import com.github.template72.loader.ResourceTemplateLoader;

import minerva.base.FileService;
import minerva.model.WorkspaceSO;

public class ExportTemplatesService {
    public static final String BOOKS = "export-template-books.html";
    public static final String BOOK = "export-template-book.html";
    public static final String PAGE = "export-template-page.html";
    public static final String TEMPLATE = "export-template.html";
    public static final String TEMPLATE_CSS = "export-template.css";
    private final WorkspaceSO workspace;
    
    public ExportTemplatesService(WorkspaceSO workspace) {
        this.workspace = workspace;
    }

    public String loadTemplate(String dn) {
        File file = new File(workspace.getFolder(), dn);
        if (file.isFile()) {
            return FileService.loadPlainTextFile(file);
        }
        return loadBuiltInTemplate(dn); // fallback
    }
    
    private String loadBuiltInTemplate(String dn) {
        return ResourceTemplateLoader.loadResource(getClass(), "/templates/export/" + dn, "UTF-8");
    }
}
