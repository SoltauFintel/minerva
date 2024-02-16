package minerva.export.template;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.base.NLS;
import minerva.base.StringService;
import minerva.workspace.WAction;

/**
 * Add or copy export template set
 */
public class AddExportTemplateSetAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");

        ExportTemplatesService x = new ExportTemplatesService(workspace);
        ExportTemplateSet template;
        if (StringService.isNullOrEmpty(id)) {
            // Create new export template set and go to edit mode.
            template = x.createFromBuiltIn();
        } else {
            // Copy existing e.t.s. and go to edit mode.
            template = x.load(id);
            template.setId(IdGenerator.createId6());
            String copy = NLS.get(user.getGuiLanguage(), "copy");
            template.setName(template.getName() + " - " + copy + " " + template.getId());
        }
        x.save(template);

        ctx.redirect("/ets/" + esc(branch) + "/edit/" + template.getId());
    }
}
