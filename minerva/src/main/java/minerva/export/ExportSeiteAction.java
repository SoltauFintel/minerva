package minerva.export;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.publish.PublishAction;
import minerva.seite.SAction;

// Eine Seite kann auf eine Seite verweisen, die es im Export nicht gibt! Eigentlich kann man nur ein Buch
// (oder mehrere) exportieren! -> Ich mach da nichts dran. Da muss der Anwender mit leben.
public class ExportSeiteAction extends SAction {

    @Override
    protected void execute() {
        String lang = ctx.queryParam("lang");
        String customer = ctx.queryParam("customer");

        String info = branch + " | " + bookFolder
		                + " | exporting page \"" + seite.getSeite().getTitle().getString(lang)
		                + "\" for customer \"" + customer + "\" and language \"" + lang + "\"";
		Logger.info(user.getLogin() + " | " + info);
        user.log(info);

        File outputFolder = GenericExportService.getService(book.getWorkspace(), customer, lang, ctx).saveSeite(seite);
        
        PublishAction.downloadFolderAsZip(outputFolder, ctx);
    }
}
