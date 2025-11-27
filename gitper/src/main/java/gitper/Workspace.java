package gitper;

import gitper.access.DirAccess;

public interface Workspace {

    String getBranch();
    
    String getFolder();

    User user();
    
    DirAccess dao();

    void onPush();

    void pull();
}
