package minerva.exclusions;

import gitper.base.StringService;
import minerva.base.NLS;

public class Visible {
    private final boolean visible;
    private final boolean hasSubpages;
    private final boolean showAllPages;
    private final boolean noTree;
    private final String reason; // RB key + "|" + $v value
    
    public Visible(boolean noTree, boolean visible, String reason) {
        this(noTree, visible, false, false, reason);
    }
    
    public Visible(boolean noTree, boolean visible, boolean hasSubpages, boolean showAllPages, String reason) {
        this.noTree = noTree;
        if ((!visible && hasSubpages) || (!visible && showAllPages) || (hasSubpages && showAllPages)) {
            throw new IllegalArgumentException(
                    "illegal combination: " + visible + "/" + hasSubpages + "/" + showAllPages);
        }
        this.visible = visible;
        this.hasSubpages = hasSubpages;
        this.showAllPages = showAllPages;
        this.reason = reason;
    }

    /**
     * @return true: no tree entry for page, false: normal tree entry for page
     */
    public boolean isNoTree() {
        return noTree;
    }

    /**
     * @return true: page is visible or page has non-empty subpages or show-all-pages-mode is active;
     * <br/> false: page is not visible.
     */
    public boolean isVisible() {
        return visible;
    }

    public boolean hasSubpages() {
        return hasSubpages;
    }

    public boolean isShowAllPages() {
        return showAllPages;
    }

    public String getReason(String guiLanguage) {
        if (StringService.isNullOrEmpty(reason)) {
            return "";
        }
        int o = reason.indexOf("|");
        return o >= 0
                ? NLS.get(guiLanguage, reason.substring(0, o)).replace("$v", reason.substring(o + 1))
                : NLS.get(guiLanguage, reason);
    }
}
