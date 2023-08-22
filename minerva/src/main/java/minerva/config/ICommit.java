package minerva.config;

import java.util.List;

public interface ICommit {

    String getHash();
    
    String getHash7();
    
    String getMessage();
    
    String getAuthor();
    
    String getCommitDateTime();
    
    List<String> getFiles();
}
