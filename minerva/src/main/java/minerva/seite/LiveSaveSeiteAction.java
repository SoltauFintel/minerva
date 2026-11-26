package minerva.seite;

/**
 * Save current page inputs to the journal every 15 seconds if something has changed in the content.
 */
public class LiveSaveSeiteAction extends SAction {

    @Override
    protected void execute() {
        try {
            final String data = ctx.fullPath() + " " + ctx.body();
            var t = new Thread(() -> user.getJournal().livesave(branch, id, data));
            t.setDaemon(true);
            t.start();
        } catch (Exception ignore) {
        }
    }
}
