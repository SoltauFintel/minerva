package minerva.seite.actions;

import org.pmw.tinylog.Logger;

import gitper.access.CommitMessage;
import minerva.seite.SAction;

public class SaveEditorsNoteAction extends SAction {

    @Override
    protected void execute() {
        String text = ctx.formParam(EditorsNoteModal.ID).trim();
        
        Logger.info(seite.getLogLine(null) + " | Editor's note: \"" + text + "\"");
        
        seite.getSeite().setEditorsNote(text);
        seite.saveMeta(new CommitMessage(seite.getTitle() + ": Editor's note"));
        
        ctx.redirect(viewlink);
    }
}
