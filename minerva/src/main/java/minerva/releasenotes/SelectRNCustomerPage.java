package minerva.releasenotes;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.book.BPage;

/**
 * Select customer for importing release notes
 */
@Deprecated
public class SelectRNCustomerPage extends BPage {

    @Override
    protected void execute() {
        if (!MinervaWebapp.factory().getConfig().isDevelopment()) {
            MinervaWebapp.factory().gitlabOnlyPage();
        }
        header(n("loadReleaseNotes"));
        DataList list = list("configs");
        for (ReleaseNotesConfig c : MinervaWebapp.factory().getConfig().loadReleaseNotesConfigs()) {
            DataMap map = list.add();
            map.put("customer", esc(c.getCustomer()));
            map.put("spaceKey", esc(c.getSpaceKey()));
            map.put("link", esc(booklink + "/rn-select-release?s=" + u(c.getSpaceKey())));
        }
    }
}
