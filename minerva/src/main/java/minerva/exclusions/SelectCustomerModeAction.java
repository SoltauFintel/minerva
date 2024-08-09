package minerva.exclusions;

import java.util.Set;

import org.pmw.tinylog.Logger;

import minerva.workspace.WAction;

/**
 * Vorher gab es nur CustomerModePage. Wenn man dann oft hin- und herumschaltet,
 * wurde die Seite (CSS, CORS error) nicht mehr richtig geladen/angezeigt. Daher
 * dieser Zwischenschritt mit redirect.
 */
public class SelectCustomerModeAction extends WAction {
    // The customer mode setting is not branch-specific, it is user specific. However, we need the exclusions
    // from the workspace for displaying the customers.

    @Override
    protected void execute() {
        String selection = ctx.pathParam("customer");
        
        String ccm = user.getUser().getCustomerMode(); // current customer mode
        Set<String> customers = new Exclusions(new ExclusionsSO(workspace).get()).getCustomers();
        if ("null".equals(selection)) { // turn mode off
            ccm = null;
            Logger.info(user.getLogin() + " | customer mode: " + ccm);
            user.saveCustomerMode(ccm);
        } else {
            for (String customer : customers) {
                if (customer.equalsIgnoreCase(selection)) {
                    ccm = customer;
                    Logger.info(user.getLogin() + " | customer mode: " + ccm);
                    user.saveCustomerMode(ccm);
                    break;
                }
            }
        }
        
        ctx.redirect("/w/" + branch + "/customer-mode");
    }
}
