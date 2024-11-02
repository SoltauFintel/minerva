package minerva.seite;

import java.util.List;

import gitper.access.CommitMessage;
import minerva.model.SeiteSO;

public interface IPageChangeStrategy {

    void set(String comment, SeiteSO seite);
    
    CommitMessage getCommitMessage(String comment, SeiteSO seite);
    
    List<PageChange> getChanges(SeiteSO seite);
}
