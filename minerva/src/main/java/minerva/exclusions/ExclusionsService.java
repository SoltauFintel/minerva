package minerva.exclusions;

import java.util.Set;

import minerva.exclusions.Exclusions.LabelClass;
import minerva.model.SeiteSO;

/**
 * Call setExclusions, setCustomer and setTags-or-setSeite and then call isAccessible.
 */
public class ExclusionsService {
    private Exclusions exclusions;
    private String customer;
    private Set<String> tags;
    
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
    
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    /**
     * setTags alternative
     * @param seite -
     */
    public void setSeite(SeiteSO seite) {
        setTags(seite.getSeite().getTags());
    }

    public boolean isAccessible() {
        return isAccessible(exclusions, tags, customer);
    }

    public boolean isAccessible(Set<String> tags) {
        setTags(tags);
        return isAccessible();
    }
    
    static boolean isAccessible(Exclusions exclusions, Set<String> tags, String pCustomer) {
        if ("-".equals(pCustomer)) {
            return true;
        }
        boolean ret = true;
        boolean voteForON = false;
        for (String tag : tags) {
            LabelClass v = exclusions.contains(tag, pCustomer);
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
