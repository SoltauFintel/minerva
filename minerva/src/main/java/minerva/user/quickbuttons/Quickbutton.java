package minerva.user.quickbuttons;

public class Quickbutton {
	private String link;
	private String label;
	/** true: no other user can take this quick button */
	private boolean onlyMe = false;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isOnlyMe() {
		return onlyMe;
	}

	public void setOnlyMe(boolean onlyMe) {
		this.onlyMe = onlyMe;
	}
}
