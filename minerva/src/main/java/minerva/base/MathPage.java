package minerva.base;

import org.pmw.tinylog.Logger;

import minerva.user.UPage;

public class MathPage extends UPage {

    @Override
    protected void execute() {
        Logger.info(user.getUser().getLogin() + " | Math");
        put("title", n("formulaEditor"));
        put("formula", "\\(x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}\\)");
    }
}
