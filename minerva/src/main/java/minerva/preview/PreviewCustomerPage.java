package minerva.preview;

import java.util.Set;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class PreviewCustomerPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        
        WorkspaceSO workspace = user.getWorkspace(branch);
        Set<String> customers = workspace.getExclusions().getCustomers();
        String bookFolder0 = workspace.getBooks().get(0).getBook().getFolder();

        header(n("preview"));
        put("branch", esc(branch));
        put("bookFolder0", esc(bookFolder0));
        DataList list = list("customers");
        for (String customer : customers) {
            DataMap map = list.add();
            map.put("customer", esc(customer.toLowerCase()));
            map.put("CUSTOMER", esc(customer.toUpperCase()));
        }
        DataList list2 = list("langs");
        for (String lang : langs) {
            DataMap map2 = list2.add();
            map2.put("lang", lang.toLowerCase());
            map2.put("LANG", lang.toUpperCase());
        }
    }
}
