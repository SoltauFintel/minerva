package minerva.mask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.base.StringService;
import minerva.base.UserMessage;
import minerva.book.BookPage;
import minerva.mask.FeatureRelationsService.Relation;
import minerva.mask.field.MaskField;
import minerva.mask.field.MaskFieldType;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.seite.SPage;

public class EditFeatureFieldsPage extends SPage {
	public static EditFeatureFieldListener editFeatureFieldListener = fields -> {};
	
    @Override
    protected void execute() {
        MaskAndDataFields mad = new MaskAndDataFields(seite);
        FeatureFieldsService sv = new FeatureFieldsService();
        FeatureFields ff = sv.get(seite);
        List<Relation> seiten = new FeatureRelationsService().getRelations(seite, ff); 
        if (isPOST()) {
        	saveFeatureFields(mad);
            saveRelations(sv, ff, seiten);
            ctx.redirect(viewlink);
        } else {
			Logger.info(user.getLogin() + " | " + branch + " | Edit feature fields+relations " + id + " " + seite.getTitle());
            BookPage.oneLang(model, book);
            header(seite.getTitle());
            setMultiselectPageMode();
            put("titel", esc(seite.getTitle()));
            put("featureFields", FeatureFieldsHtmlFactory.FACTORY.build(seite, true).html());
            mad.customersMultiselect(model);
            
            // Relations
            DataList list = list("seiten");
            for (Relation s : seiten) {
                DataMap map = list.add();
                map.put("id", s.getId());
                map.put("icon", s.getIcon());
                map.put("title", esc(s.getTitle()));
                map.put("isDeletable", s.isDeletable());
            }
            put("hasSeiten", !seiten.isEmpty());
        }
    }

	private void saveFeatureFields(MaskAndDataFields mad) {
		List<FeatureFieldChange> fields = new ArrayList<>();
		for (MaskField maskField : mad.getMaskFields()) {
		    if (!maskField.isImportField()) {
		        String id = maskField.getId();
		        String oldValue = mad.getDataFields().get(id);
		        String value = getValue(maskField);
		        uniqueness(maskField, value, oldValue, mad);
		        mad.getDataFields().set(id, value);
		        fields.add(new FeatureFieldChange(id, oldValue, mad.getDataFields().get(id)/*set() could change value!*/));
		    }
		}
		mad.save();
		editFeatureFieldListener.changed(fields);
	}

    private String getValue(MaskField maskField) {
        if (MaskFieldType.CUSTOMERS.equals(maskField.getType())) {
            String[] a = ctx.req.queryParamsValues(maskField.getId());
            return a == null ? "" : Arrays.asList(a).stream().collect(Collectors.joining(","));
        }
        String value = ctx.formParam(maskField.getId());
        if (value != null) {
            value = value.trim();
        }
        if (MaskFieldType.BOOL.equals(maskField.getType())) {
            value = "on".equals(value) ? "true" : "false";
        } else if (MaskFieldType.INTEGER.equals(maskField.getType())) {
            value = toInteger(value);
        }
        return value;
    }

    private String toInteger(String value) {
        String num = "";
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= '0' && c <= '9') {
                num += c;
            } else {
                break;
            }
        }
        return num;
    }
    
    private void uniqueness(MaskField maskField, String value, String oldValue, MaskAndDataFields mad) {
        boolean mustBeUnique = MaskFieldType.UNIQUE.equals(maskField.getType());
        boolean dirty = !value.isBlank() && !value.equals(oldValue);
        if (mustBeUnique && dirty && mad.findValue(seite, maskField.getId(), value)) {
            throw new UserMessage("valueIsntUnique", seite.getBook().getWorkspace(), s -> s.replace("$v", value).replace("$l", maskField.getLabel()));
        }
    }
    
	public static class FeatureFieldChange {
		private final String id;
		private final String oldValue;
		private final String newValue;

		public FeatureFieldChange(String id, String oldValue, String newValue) {
			super();
			this.id = id;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public String getId() {
			return id;
		}

		public String getOldValue() {
			return oldValue;
		}

		public String getNewValue() {
			return newValue;
		}
	}
    
    public interface EditFeatureFieldListener {

    	void changed(List<FeatureFieldChange> fields);
    }
    
    private void saveRelations(FeatureFieldsService sv, FeatureFields ff, List<Relation> seiten) {
        List<String> pages = get("pages");
        List<String> links = get("links");
        
		boolean dirty = !pages.isEmpty() || !links.isEmpty();
        for (Relation s : seiten) {
            if ("on".equals(ctx.formParam(s.getId()))) {
                s.deleteFrom(ff);
                dirty = true;
            }
        }
        
        if (dirty) {
            validate(pages, links);
            ff.getPages().addAll(pages);
            ff.getLinks().addAll(links);
            sv.set(seite, ff);
        }
    }

    private List<String> get(String name) {
        String w = ctx.formParam(name);
        if (StringService.isNullOrEmpty(w)) {
            return List.of();
        }
        return Arrays.asList(w.split("\n")).stream().filter(i -> !i.isBlank()).map(i -> i.trim()).collect(Collectors.toList());
    }

    private void validate(List<String> pages, List<String> links) {
        for (int i = 0; i < pages.size(); i++) {
            String id = pages.get(i);
            
            if (!findPage(id)) {
                String idByTitle = isTitle(id);
                if (idByTitle == null) {
                    throw new UserMessage("pageNotFound3", workspace, s -> s.replace("$id", id));
                } else {
                    pages.set(i, idByTitle);
                }
            }
        }
        for (String link : links) {
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                throw new UserMessage("linkMustStartWithHttp", workspace);
            }
        }
    }
    
    private String isTitle(String id) {
        List<String> ret = new ArrayList<>();
        for (BookSO book : workspace.getBooks()) {
            SeiteSO x = book.getSeiten()._byTitle(id, "de");
            if (x != null) {
                ret.add(x.getId());
            }
        }
        if (ret.size() == 1) {
            return ret.get(0);
        }
        return null;
    }

    private boolean findPage(String id) {
        return workspace.findPage(id) != null;
    }
}
