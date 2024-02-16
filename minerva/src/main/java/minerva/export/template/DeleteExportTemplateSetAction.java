package minerva.export.template;

import minerva.workspace.WAction;

public class DeleteExportTemplateSetAction extends WAction {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        
        new ExportTemplatesService(workspace).delete(id);

        ctx.redirect("/ets/" + branch);
    }
}
