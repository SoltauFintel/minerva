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
    private String context = ""; // e.g. "PDF"
    
    public Exclusions getExclusions() {
        return exclusions;
    }

    public void setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions;
    }

    /**
     * @param customer "-" for no specific customer
     */
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
        return isAccessible(exclusions, tags, customer, context);
    }

    public boolean isAccessible(Set<String> tags) {
        setTags(tags);
        return isAccessible();
    }
    
    static boolean isAccessible(Exclusions exclusions, Set<String> tags, String pCustomer, String context) {
        if ("-".equals(pCustomer)) {
            return true;
        }
        boolean ret = true;
        boolean voteForON = false;
        boolean nicht_drucken = "PDF".equalsIgnoreCase(context);
        for (String tag : tags) {
        	LabelClass v;
        	if (nicht_drucken && "nicht_drucken".equals(tag)) {
        		v = LabelClass.OFF;
        	} else {
        		v = exclusions.contains(tag, pCustomer);
        	}
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

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
