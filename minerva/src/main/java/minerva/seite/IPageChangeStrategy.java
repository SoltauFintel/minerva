package minerva.seite;

import java.util.List;

import minerva.model.SeiteSO;
import minerva.persistence.gitlab.git.CommitMessage;

public interface IPageChangeStrategy {

    void set(String comment, SeiteSO seite);
    
    CommitMessage getCommitMessage(String comment, SeiteSO seite);
    
    List<PageChange> getChanges(SeiteSO seite);
}
