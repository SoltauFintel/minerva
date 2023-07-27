package minerva.seite;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import minerva.access.CommitMessage;

public class HelpKeysPage extends SPage {

    @Override
    protected void execute() {
        if (isPOST()) {
            String helpKeys = ctx.formParam("helpKeys");
            saveHelpKeys(helpKeys);
            ctx.redirect(viewlink);
        } else {
            header(n("helpKeys"));
            put("helpKeys", seite.getSeite().getHelpKeys().stream().collect(Collectors.joining("\n")) + "\n");
        }
    }

    private void saveHelpKeys(String helpKeysText) {
        List<String> helpKeysTarget = seite.getSeite().getHelpKeys();
        helpKeysTarget.clear();
        for (String line : helpKeysText.split("\n")) {
            String helpKey = line.trim();
            if (!helpKey.isEmpty()) {
                helpKeysTarget.add(helpKey);
            }
        }
        Collections.sort(helpKeysTarget);
        seite.saveMeta(new CommitMessage(seite, "help keys"));
        seite.updateOnlineHelp();
    }
}
