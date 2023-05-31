package minerva.persistence.gitlab;

import minerva.user.User;

public class GitlabUser extends User {
	private final String password;
	private final String mail;
	
	public GitlabUser(String login, String password, String mail) {
		super(login);
		this.password = password;
		this.mail = mail;
	}

	public String getPassword() {
		return password;
	}

	public String getMail() {
		return mail;
	}
}
