package minerva.export.template;

import minerva.workspace.WAction;

/**
 * Add or copy export template set
 */
public class AddExportTemplateSetAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");

        String templateId = new ExportTemplatesService(workspace).addOrCopyTemplateSet(id);

        ctx.redirect("/ets/" + esc(branch) + "/edit/" + esc(templateId));
    }
}
