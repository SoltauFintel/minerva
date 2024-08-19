package minerva.exclusions;

public class Visible {
	private final boolean visible;
	private final boolean hasSubpages;
	private final boolean showAllPages;
	private final boolean noTree;
	
	public Visible(boolean noTree, boolean visible) {
		this(noTree, visible, false, false);
	}
	
	public Visible(boolean noTree, boolean visible, boolean hasSubpages, boolean showAllPages) {
	    this.noTree = noTree;
		if ((!visible && hasSubpages) || (!visible && showAllPages) || (hasSubpages && showAllPages)) {
			throw new IllegalArgumentException(
					"illegal combination: " + visible + "/" + hasSubpages + "/" + showAllPages);
		}
		this.visible = visible;
		this.hasSubpages = hasSubpages;
		this.showAllPages = showAllPages;
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
}
