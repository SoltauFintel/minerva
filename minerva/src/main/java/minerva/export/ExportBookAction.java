package minerva.export;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.book.BAction;

public class ExportBookAction extends BAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        Logger.info(user.getUser().getLogin() + " | " + branch + " | " + bookFolder
                + " | export book for customer " + customer + " and language " + lang);
        
        File tf = new File("export/book");
        FileService.deleteFolder(tf);
        ExportService x = new ExportService(lang);
        x.initExclusionsService(book.getWorkspace(), customer);
        x.save(book, tf);

        // TODO Download as zip
    }
}
