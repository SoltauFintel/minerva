package minerva.exclusions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;

import minerva.base.StringService;
import minerva.model.ExclusionsSO;
import minerva.user.CustomerMode;
import minerva.workspace.WPage;

/**
 * With activated customer mode only the pages of the specified customer will be displayed.
 */
public class CustomerModePage extends WPage {
    // The customer mode setting is not branch-specific, it is user specific. However, we need the exclusions
    // from the workspace for displaying the customers.

    @Override
    protected void execute() {
        CustomerMode ccm = user.getCustomerMode(); // current customer mode
        Exclusions exclusions = new Exclusions(new ExclusionsSO(workspace).get());
        Set<String> customers = exclusions.getCustomers();
        List<String> tags = exclusions.getTags(ccm.getCustomer());
        
        header(n("customerMode"));
        put("ccm", esc(ccm.toString()));
        DataList list = list("customers");
        for (String customer : customers) {
            list.add()
                .put("customer", customer.toLowerCase())
                .put("label", customer.toUpperCase())
                .put("css", customer.equalsIgnoreCase(ccm.getCustomer()) ? "btn-success" : "btn-default");
        }
        list.add()
            .put("customer", "null")
            .put("label", n(StringService.isNullOrEmpty(ccm.getCustomer()) ? "customerModeIsOff" : "turnOffCustomerMode"))
            .put("css", StringService.isNullOrEmpty(ccm.getCustomer()) ? "btn-danger" : "btn-default");
        if (tags == null) {
            put("showTags", false);
        } else {
            put("showTags", true);
            put("tagsPlus", esc(tags.stream().filter(i -> i.startsWith("+")).map(i -> i.substring(1)).collect(Collectors.joining(", "))));
            put("tagsMinus", esc(tags.stream().filter(i -> i.startsWith("-")).map(i -> i.substring(1)).collect(Collectors.joining(", "))));
            put("tags", esc(tags.stream().filter(i -> !i.startsWith("-") && !i.startsWith("+")).collect(Collectors.joining(", "))));
        }
    }
}
