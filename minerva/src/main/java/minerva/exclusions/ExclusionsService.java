package minerva.exclusions;

import java.util.Set;

import minerva.exclusions.Exclusions.LabelClass;

public class ExclusionsService {
    private String customer;
    private Set<String> labels;
    private Exclusions exclusions;
    
    public Exclusions getExclusions() {
        return exclusions;
    }

    public void setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }
    
    public void setTags(Set<String> labels) {
        this.labels = labels;
    }

    public boolean isAccessible() {
        return isAccessible(exclusions, labels, customer);
    }

    public boolean isAccessible(Set<String> tags) {
        setTags(tags);
        return isAccessible();
    }
    
    static boolean isAccessible(Exclusions exclusions, Set<String> labels, String pCustomer) {
        if ("-".equals(pCustomer)) {
            return true;
        }
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
