package minerva.base;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.user.UPage;

public class MathPage extends UPage {
    // TODO Zoom Trigger über DblClick machen

    @Override
    protected void execute() {
        Logger.info(user.getUser().getLogin() + " | Math");

        String formula = "x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}";
        
        put("title", n("formulaEditor"));
        put("formula", "\\(" + formula + "\\)");
        put("formula2", formula);
        put("serviceUrl", MinervaWebapp.factory().getConfig().getMathJaxConverterURL(""));
    }
}
