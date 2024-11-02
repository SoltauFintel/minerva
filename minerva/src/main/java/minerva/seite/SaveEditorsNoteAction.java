package minerva.seite;

import org.pmw.tinylog.Logger;

import gitper.access.CommitMessage;

public class SaveEditorsNoteAction extends SAction {

    @Override
    protected void execute() {
        String text = ctx.formParam("editorsnote").trim();
        
        Logger.info(user.getLogin() + " | \"" + seite.getTitle() + "\" | editor's note: " + text);
        
        seite.getSeite().setEditorsNote(text);
        seite.saveMeta(new CommitMessage("Editor's note"));
        
        ctx.redirect(viewlink);
    }
}
