package minerva.export;

import org.pmw.tinylog.Logger;

import minerva.model.WorkspaceSO;
import minerva.workspace.WAction;

public class ExportWorkspaceAction extends WAction {

    @Override
    protected void execute() {
        // params see ExportRequest!
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        String info = branch + " | exporting all books for customer \"" + customer + "\" and language \"" + lang + "\"";
        Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        WorkspaceSO workspace = user.getWorkspace(branch);
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        String id = GenericExportService.getService(new ExportRequest(workspace, ctx)).getBooksExportDownloadId(workspace);
        
        DownloadExportPage.redirectToThisPage(ctx, branch, id);
    }
}
