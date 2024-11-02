package minerva.mask.field;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import gitper.base.StringService;
import minerva.base.NLS;
import minerva.mask.Mask;
import minerva.mask.MasksService;
import minerva.workspace.WPage;

public class AddMaskFieldPage extends WPage {

    @Override
    protected void execute() {
        String tag = ctx.pathParam("tag");
        put("tag", esc(tag));
        if (isPOST()) {
            if (StringService.isNullOrEmpty(ctx.formParam("id"))) {
                throw new RuntimeException("Bitte ID eingeben!");
            } else if (StringService.isNullOrEmpty(ctx.formParam("label"))) {
                throw new RuntimeException("Bitte Label eingeben!");
            }
            MasksService sv = new MasksService(workspace);
            Mask mask = sv.getMask(tag);
            if (mask == null) {
                throw new RuntimeException("Mask " + tag + " doesn't exist!");
            }
            MaskField f = new MaskField();
            f.setId(ctx.formParam("id"));
            f.setLabel(ctx.formParam("label"));
            f.setImportField("on".equals(ctx.formParam("importField")));
            f.setType(MaskFieldType.valueOf(ctx.formParam("type")));
            mask.getFields().add(f);
            sv.saveMask(mask);
            ctx.redirect("/mask/" + branch + "/" + tag);
        } else {
            combobox_idAndLabel("types", types(user.getGuiLanguage()), MaskFieldType.TEXT.name(), false);
            header(n("addMaskField"));
        }
    }

    public static List<IdAndLabel> types(String lang) {
        List<IdAndLabel> types = new ArrayList<>();
        for (MaskFieldType type : MaskFieldType.values()) {
            types.add(new MaskFieldTypeIAL(type, lang));
        }
        return types;
    }
    
    public static class MaskFieldTypeIAL implements IdAndLabel {
        private final MaskFieldType type;
        private final String guiLanguage;
        
        public MaskFieldTypeIAL(MaskFieldType type, String guiLanguage) {
            this.type = type;
            this.guiLanguage = guiLanguage;
        }

        @Override
        public String getId() {
            return type.name();
        }

        @Override
        public String getLabel() {
            return NLS.get(guiLanguage, "maskFieldType_" + type.name());
        }
    }
}
