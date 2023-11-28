package minerva.export;

import org.pmw.tinylog.Logger;

import minerva.model.WorkspaceSO;
import minerva.workspace.WAction;

public class ExportWorkspaceAction extends WAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");
        String template = ctx.queryParam("template");

        String info = branch + " | exporting books for customer \"" + customer + "\" and language \"" + lang + "\"";
		Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        WorkspaceSO workspace = user.getWorkspace(branch);
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        String id = GenericExportService.getService(workspace, customer, lang, template, ctx).getBooksExportDownloadId(workspace);
        
        ctx.redirect("/w/" + esc(branch) + "/download-export/" + id);
    }
}
