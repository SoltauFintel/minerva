package minerva.export;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.model.WorkspaceSO;
import minerva.publish.PublishAction;
import minerva.user.UAction;

public class ExportWorkspaceAction extends UAction {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        String info = branch + " | exporting books for customer \"" + customer + "\" and language \"" + lang + "\"";
		Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        WorkspaceSO workspace = user.getWorkspace(branch);
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        File outputFolder = GenericExportService.getService(workspace, customer, lang, ctx).saveWorkspace(workspace);
        
        PublishAction.downloadFolderAsZip(outputFolder, ctx);
    }
}
