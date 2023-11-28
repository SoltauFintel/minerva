package minerva.export.template;

import static minerva.base.StringService.umlaute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.github.template72.loader.ResourceTemplateLoader;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.access.CommitMessage;
import minerva.access.MultiPurposeDirAccess;
import minerva.model.WorkspaceSO;

public class ExportTemplatesService {
    private static final String BOOKS = "export-template-books.html";
    private static final String BOOK = "export-template-book.html";
    private static final String PAGE = "export-template-page.html";
    private static final String TEMPLATE = "export-template.html";
    private static final String TEMPLATE_CSS = "export-template.css";
    private static final String PDF_CSS = "pdf-template.css";
    private static final String DN_EXT = ".txt";
    private final WorkspaceSO workspace;
    
    public ExportTemplatesService(WorkspaceSO workspace) {
        this.workspace = workspace;
    }
    
    private String objectToString(ExportTemplateSet set) { // serialize
    	return new ExportTemplateSetFile().serialize(set);
    }
    
    private ExportTemplateSet stringToObject(String data) { // deserialize
    	return new ExportTemplateSetFile().deserialize(data);
    }
    
    public List<ExportTemplateSet> loadAll() {
    	List<ExportTemplateSet> ret = new ArrayList<>();
    	Map<String, String> files = workspace.dao().loadAllFiles(workspace.getFolder() + "/export-templates");
    	for (Entry<String, String> e : files.entrySet()) {
			if (e.getKey().endsWith(DN_EXT)) {
				ret.add(stringToObject(e.getValue()));
			}
		}
		ret.sort((a, b) -> umlaute(a.getName()).compareTo(umlaute(b.getName())));
    	return ret;
    }
    
    public ExportTemplateSet load(String id) {
		ExportTemplateSet set = stringToObject(new MultiPurposeDirAccess(workspace.dao()).load(filename(id)));
		if (set == null) {
			throw new RuntimeException("Export template set doesn't exist!");
		}
		return set;
	}

	public ExportTemplateSet createFromBuiltIn() {
    	ExportTemplateSet ret = new ExportTemplateSet();
    	ret.setId(IdGenerator.createId6());
    	ret.setName(ret.getId());
    	ret.setBooks(loadBuiltInTemplate(BOOKS));
    	ret.setBook(loadBuiltInTemplate(BOOK));
    	ret.setPage(loadBuiltInTemplate(PAGE));
    	ret.setTemplate(loadBuiltInTemplate(TEMPLATE));
    	ret.setStyles(loadBuiltInTemplate(TEMPLATE_CSS));
    	ret.setPdfStyles(loadBuiltInTemplate(PDF_CSS));
    	return ret;
    }
    
    private String loadBuiltInTemplate(String dn) {
        return ResourceTemplateLoader.loadResource(getClass(), "/templates/export/" + dn, "UTF-8");
    }

	public void save(ExportTemplateSet set) {
		CommitMessage cm = new CommitMessage("Export template set: " + set.getName());
		String dn = filename(set.getId());
		new MultiPurposeDirAccess(workspace.dao()).save(dn, objectToString(set), cm, workspace);
	}
	
	public void delete(String id) {
		ExportTemplateSet set = load(id);
		Set<String> filenames = new HashSet<>();
		filenames.add(filename(id));
		List<String> cant = new ArrayList<>();
		CommitMessage cm = new CommitMessage("Delete export template set: " + set.getName());
		workspace.dao().deleteFiles(filenames, cm, workspace, cant);
		if (cant.isEmpty()) {
			workspace.getUser().log("Export template set deleted: " + set.getName());
		} else {
			throw new RuntimeException("Can't delete export template set!");
		}
	}
	
	private String filename(String id) {
		return workspace.getFolder() + "/export-templates/" + id + DN_EXT;
	}
}
