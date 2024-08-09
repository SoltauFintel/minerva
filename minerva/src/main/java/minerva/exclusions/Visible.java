package minerva.exclusions;

public class Visible {
	private final boolean visible;
	private final boolean hasSubpages;
	private final boolean showAllPages;

	public Visible(boolean visible) {
		this(visible, false, false);
	}
	
	public Visible(boolean visible, boolean hasSubpages, boolean showAllPages) {
		if ((!visible && hasSubpages) || (!visible && showAllPages) || (hasSubpages && showAllPages)) {
			throw new IllegalArgumentException(
					"illegal combination: " + visible + "/" + hasSubpages + "/" + showAllPages);
		}
		this.visible = visible;
		this.hasSubpages = hasSubpages;
		this.showAllPages = showAllPages;
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
