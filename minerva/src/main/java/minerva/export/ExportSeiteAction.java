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

        Logger.info(user.getLogin() + " | " + branch + " | " + bookFolder
                + " | export page \"" + seite.getSeite().getTitle().getString(lang)
                + "\" for customer " + customer + " and language " + lang);
        user.log("export page #" + seite.getId() + " \"" + seite.getSeite().getTitle().getString(lang)
                + "\", " + customer + ", " + lang);

        File outputFolder = new MultiPageHtmlExportService(book.getWorkspace(), customer, lang).saveSeite(seite);
        
        PublishAction.downloadFolderAsZip(outputFolder, ctx);
    }
}
