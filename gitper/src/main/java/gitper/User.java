package gitper;

import java.util.List;

public interface User {
	
	String getLogin();
	
	void setLogin(String login);

	String getMailAddress();

	void setMailAddress(String mail);
	
	String getRealName();
	
	GitlabConfig getGitlabConfig();
	
	List<String> getDelayedPush();
}
