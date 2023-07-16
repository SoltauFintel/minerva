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

        Logger.info(user.getUser().getLogin() + " | " + branch
                + " | export books for customer " + customer + " and language " + lang);
        user.log("export all books, " + customer + ", " + lang);

        WorkspaceSO workspace = user.getWorkspace(branch);
        if (workspace.getBooks().isEmpty()) {
            throw new RuntimeException("There are no books!");
        }
        File outputFolder = new MultiPageHtmlExportService(workspace, customer, lang).saveWorkspace(workspace);
        
        PublishAction.downloadFolderAsZip(outputFolder, ctx);
    }
}
