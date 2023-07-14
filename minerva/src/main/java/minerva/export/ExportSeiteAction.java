package minerva.export;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.base.FileService;
import minerva.seite.SAction;

public class ExportSeiteAction extends SAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        Logger.info(user.getUser().getLogin() + " | " + branch + " | " + bookFolder
                + " | export page \"" + seite.getSeite().getTitle().getString(lang)
                + "\" for customer " + customer + " and language " + lang);
        
        File tf = new File("export/seite");
        FileService.deleteFolder(tf);
        ExportService x = new ExportService(lang);
        x.initExclusionsService(book.getWorkspace(), customer);
        x.save(seite, tf);
        
        // TODO Download as zip
    }
}
