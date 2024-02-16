package minerva.export;

import org.pmw.tinylog.Logger;

import minerva.book.BAction;

public class ExportBookAction extends BAction {

    @Override
    protected void execute() {
        // params see ExportRequest!
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        String info = branch + " | exporting book \"" + bookFolder + "\" for customer \"" + customer + "\" and language \"" + lang + "\"";
        Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        String id = GenericExportService.getService(new ExportRequest(book.getWorkspace(), ctx)).getBookExportDownloadId(book);
        
        DownloadExportPage.redirectToThisPage(ctx, branch, id);
    }
}
