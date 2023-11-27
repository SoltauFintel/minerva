package minerva.exclusions;

import java.util.Set;

import minerva.MinervaWebapp;
import minerva.exclusions.Exclusions.LabelClass;
import minerva.model.SeiteSO;

/**
 * Call setExclusions, setCustomer and setTags-or-setSeite and then call isAccessible.
 */
public class ExclusionsService {
    private final String[] pdf_tags = MinervaWebapp.factory().getConfig().getPDF_tags();
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
        return isAccessible(exclusions, tags, customer, context, pdf_tags);
    }

    public boolean isAccessible(Set<String> tags) {
        setTags(tags);
        return isAccessible();
    }
    
    static boolean isAccessible(Exclusions exclusions, Set<String> tags, String pCustomer, String context, String[] pdf_tags) {
        if ("-".equals(pCustomer)) {
            return true;
        }
        boolean ret = true;
        boolean voteForON = false;
        boolean pdfMode = "PDF".equalsIgnoreCase(context);
        for (String tag : tags) {
        	LabelClass v;
        	if (pdfMode && isPdfTag(tag, pdf_tags)) {
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
    
    private static boolean isPdfTag(String tag, String[] pdf_tags) {
		for (String i : pdf_tags) {
			if (i.equals(tag)) {
				return true;
			}
		}
		return false;
    }

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}
