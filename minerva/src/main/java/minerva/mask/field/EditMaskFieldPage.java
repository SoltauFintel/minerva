package minerva.mask.field;

import minerva.base.StringService;
import minerva.mask.Mask;
import minerva.mask.MasksService;
import minerva.workspace.WPage;

public class EditMaskFieldPage extends WPage {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        String id = ctx.pathParam("id");
        
        MasksService sv = new MasksService(workspace);
        Mask mask = sv.getMask(tag);
        if (mask == null) {
            throw new RuntimeException("Mask " + tag + " doesn't exist!");
        }
        MaskField f = mask.get(id);
        
        if (isPOST()) {
            if (StringService.isNullOrEmpty(ctx.formParam("id"))) {
                throw new RuntimeException("Bitte ID eingeben!");
            } else if (StringService.isNullOrEmpty(ctx.formParam("label"))) {
                throw new RuntimeException("Bitte Label eingeben!");
            }
            
            f.setId(ctx.formParam("id").trim());
            f.setLabel(ctx.formParam("label").trim());
            f.setImportField("on".equals(ctx.formParam("importField")));
            f.setType(MaskFieldType.valueOf(ctx.formParam("type")));
            sv.saveMask(mask);
            
            ctx.redirect("/mask/" + branch + "/" + tag);
        } else {
            put("tag", esc(tag));
            put("id", esc(id));
            put("label", esc(f.getLabel()));
            put("importField", f.isImportField());
            combobox_idAndLabel("types", AddMaskFieldPage.types(user.getGuiLanguage()), f.getType().name(), false);
            header(n("editMaskField"));
        }
    }
}
