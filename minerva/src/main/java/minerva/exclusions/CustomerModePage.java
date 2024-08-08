package minerva.exclusions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.template72.data.DataList;

import minerva.base.MinervaPageInitializer;
import minerva.base.StringService;
import minerva.model.ExclusionsSO;
import minerva.workspace.WPage;

/**
 * With activated customer mode only the pages of the specified customer will be displayed.
 */
public class CustomerModePage extends WPage {
    // The customer mode setting is not branch-specific, it is user specific. However, we need the exclusions
    // from the workspace for displaying the customers.

    @Override
    protected void execute() {
        String selection = ctx.pathParam("customer");
        String ccm = user.getUser().getCustomerMode(); // current customer mode
        Exclusions exclusions = new Exclusions(new ExclusionsSO(workspace).get());
        Set<String> customers = exclusions.getCustomers();
        if ("off".equals(selection)) { // turn mode off
            ccm = null;
            user.saveCustomerMode(ccm);
        } else if (!"na".equals(selection)) { // change customer?
            for (String customer : customers) {
                if (customer.equalsIgnoreCase(selection)) {
                    ccm = customer;
                    user.saveCustomerMode(ccm);
                    break;
                }
            }
        }
        List<String> tags = exclusions.getTags(ccm);
        if (tags == null) {
            put("showTags", false);
        } else {
            put("showTags", true);
            put("tagsPlus", esc(tags.stream().filter(i -> i.startsWith("+")).map(i -> i.substring(1)).collect(Collectors.joining(", "))));
            put("tagsMinus", esc(tags.stream().filter(i -> i.startsWith("-")).map(i -> i.substring(1)).collect(Collectors.joining(", "))));
            put("tags", esc(tags.stream().filter(i -> !i.startsWith("-") && !i.startsWith("+")).collect(Collectors.joining(", "))));
        }
        
        header(n("customerMode"));
        put("ccm", esc(ccm == null ? "" : ccm.toUpperCase()));
        DataList list = list("customers");
        for (String customer : customers) {
            list.add()
                .put("customer", customer.toLowerCase())
                .put("label", customer.toUpperCase())
                .put("css", customer.equalsIgnoreCase(ccm) ? "btn-success" : "btn-default");
        }
        list.add()
            .put("customer", "off")
            .put("label", n(StringService.isNullOrEmpty(ccm) ? "customerModeIsOff" : "turnOffCustomerMode"))
            .put("css", StringService.isNullOrEmpty(ccm) ? "btn-danger" : "btn-default");
        MinervaPageInitializer.customerMode(ccm, this); // This page must refresh MinervaPageInitializer values.
    }
}
