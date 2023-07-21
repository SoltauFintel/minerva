package minerva.export;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.book.BAction;
import minerva.publish.PublishAction;

public class ExportBookAction extends BAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        Logger.info(user.getLogin() + " | " + branch + " | " + bookFolder
                + " | export book for customer " + customer + " and language " + lang);
        user.log("export book " + bookFolder + ", " + customer + ", " + lang);

        File outputFolder = new MultiPageHtmlExportService(book.getWorkspace(), customer, lang).saveBook(book);
        
        PublishAction.downloadFolderAsZip(outputFolder, ctx);
    }
}
