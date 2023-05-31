package minerva.user;

import minerva.MinervaWebapp;

public class User {
	private final String login;
	/** Einstellung welche Sprache angezeigt wird */
	private String language = MinervaWebapp.factory().getLanguages().get(0);
	
	public User(String login) {
		this.login = login;
	}

	public String getLogin() {
		return login;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
}
