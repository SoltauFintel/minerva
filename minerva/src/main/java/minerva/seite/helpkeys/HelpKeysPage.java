package minerva.seite.helpkeys;

import java.util.stream.Collectors;

import minerva.seite.SPage;

/**
 * Edit help keys for page
 */
public class HelpKeysPage extends SPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            seite.saveHelpKeys(ctx.formParam("helpKeys"));
            ctx.redirect(viewlink);
        } else {
            header(n("helpKeys"));
            put("helpKeys", seite.getSeite().getHelpKeys().stream().collect(Collectors.joining("\n")) + "\n");
            setJQueryObenPageMode();
        }
    }
}
