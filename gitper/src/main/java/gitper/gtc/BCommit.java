package gitper.gtc;

public class BCommit {
	private String shortMessage;
	private String autorInitialien;
	private String authoredDate;
	private String committerInitialien;
	private String commitDate;
	/** changes ID */
	private String cid;

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getAutorInitialien() {
		return autorInitialien;
	}

	public void setAutorInitialien(String autorInitialien) {
		this.autorInitialien = autorInitialien;
	}

	public String getAuthoredDate() {
		return authoredDate;
	}

	public void setAuthoredDate(String authoredDate) {
		this.authoredDate = authoredDate;
	}

	public String getCommitterInitialien() {
		return committerInitialien;
	}

	public void setCommitterInitialien(String committerInitialien) {
		this.committerInitialien = committerInitialien;
	}

	public String getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
}
