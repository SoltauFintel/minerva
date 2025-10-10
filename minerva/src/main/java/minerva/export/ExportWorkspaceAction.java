package minerva.export;

import org.pmw.tinylog.Logger;

import minerva.base.MinervaMetrics;
import minerva.model.WorkspaceSO;
import minerva.workspace.WAction;

/**
 * Export all books except feature tree and internal books
 */
public class ExportWorkspaceAction extends WAction {

    @Override
    protected void execute() {
        // params see ExportRequest!
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        String info = branch + " | ExportWorkspaceAction: exporting all books except feature tree and internal books"
        		+ " for customer \"" + customer + "\" and language \"" + lang + "\"";
        Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        WorkspaceSO workspace = user.getWorkspace(branch);
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        String id = GenericExportService.getService(new ExportRequest(workspace, ctx)).getBooksExportDownloadId(workspace);
        
        MinervaMetrics.EXPORT.inc();
        DownloadExportPage.redirectToThisPage(ctx, branch, id);
    }
}
