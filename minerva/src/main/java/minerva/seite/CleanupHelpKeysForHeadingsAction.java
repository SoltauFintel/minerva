package minerva.seite;

import org.pmw.tinylog.Logger;

import minerva.access.CommitMessage;
import ohhtml.toc.TocMacro;

/**
 * Wird eine Überschrift geändert, so sind zuvor damit verknüpfte Hilfe-Keys verwaist.
 * Diese Funktion löscht diese verwaisten Einträge.
 */
public class CleanupHelpKeysForHeadingsAction extends SAction {

    @Override
    protected void execute() {
        Logger.info(user.getLogin() + " | " + branch + " | " + seite.getTitle() + ": cleanup help-keys for headings");

        boolean dirty = false;
        for (String lang : langs) {
            TocMacro macro = new TocMacro(seite.getTocMacroPage(), "-", lang, "");
            macro.setSeite(new SeiteIPageAdapter(seite));
            if (macro.cleanupHkhErrors()) {
                dirty = true;
            }
        }
        if (dirty) {
            seite.saveMeta(new CommitMessage(seite, "cleanup help keys for headings"));
        }
        
        ctx.redirect(viewlink);
    }
}
