package minerva.mask;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.mask.field.MaskField;
import minerva.workspace.WPage;

/**
 * View mask, edit mask fields
 */
public class ViewMaskPage extends WPage {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        put("tag", esc(tag));
        
        Mask mask = new MasksService(workspace).getMask(tag);
        if (mask == null) {
            throw new RuntimeException("Mask " + tag + " doesn't exist!");
        }
        
        header(n("mask") + " " + tag);
        DataList list = list("fields");
        int max = mask.getFields().size() - 1;
        for (int i = 0; i <= max; i++) {
            MaskField f = mask.getFields().get(i);
            DataMap map = list.add();
            map.put("id", esc(f.getId()));
            map.put("label", esc(f.getLabel()));
            map.put("importField", f.isImportField());
            map.put("type", n("maskFieldType_" + f.getType().name()));
            map.put("upAllowed", i > 0);
            map.put("downAllowed", i < max);
        }
        putSize("n", mask.getFields());
        MasksPage.masksMenu(model, branch, n("masks"));
    }
}
