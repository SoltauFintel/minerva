package gitper.gtc;

import java.io.File;

public interface RepositoryDefinition {

	String getUrl();
	
	String getUser();
	
	String getPassword();
	
	File getLocalFolder();
}
