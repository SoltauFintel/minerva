package minerva.auth;

import minerva.user.User;

public interface LoginService {

	boolean withPassword();
	
	/**
	 * @param login -
	 * @param password -
	 * @return null if login failed
	 */
	User login(String login, String password);
}
