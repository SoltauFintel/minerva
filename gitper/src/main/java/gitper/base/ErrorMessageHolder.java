package gitper.base;

import java.util.Map;

public interface ErrorMessageHolder {

	String getKey();
	
	Map<String, String> getParameters();
}
