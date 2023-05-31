package minerva.migration;

import java.util.Set;

import minerva.migration.OldExclusions.LabelClass;

public class OldExclusionsService {
    private String customer;
    private Set<String> labels;

    public void setCustomer(String customer) {
        if (customer == null) {
            throw new IllegalArgumentException("customer must not be null in ExclusionsService.setCustomer()!"
                    + " This can happen if there was no mapping table request.");
        } else if (customer.contains("-")) {
            throw new IllegalArgumentException("ExclusionsService.setCustomer(\"" + customer + "\"): A '-' in the customer name is not allowed!");
        }
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }
    
    public void setContent(Set<String> labels) {
        this.labels = labels;
    }

    public boolean isAccessible() {
        return isAccessible(OldExclusions.getInstance(), labels, customer);
    }
    
    static boolean isAccessible(OldExclusions exclusions, Set<String> labels, String pCustomer) {
        boolean ret = true;
        boolean voteForON = false;
        for (String label : labels) {
            LabelClass v = exclusions.contains(label, pCustomer);
            if (v == LabelClass.ON) {
                voteForON = true;
            } else if (v == LabelClass.OFF) {
                return false;
            } else if (v == LabelClass.NORMAL) {
                ret = false;
            }
        }
        return voteForON || ret;
    }
}
