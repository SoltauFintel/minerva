package minerva.releasenotes;

import minerva.seite.SAction;

/**
 * Re-import release notes
 */
public class ReleaseNotesReimportAction extends SAction {

    @Override
    protected void execute() {
        String newId = ReleaseNotesService2.reimport(seite, langs);
        ctx.redirect(viewlinkWithoutId + newId);
    }
}
