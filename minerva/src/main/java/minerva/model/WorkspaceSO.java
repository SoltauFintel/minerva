package minerva.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import minerva.access.DirAccess;
import minerva.seite.tag.TagNList;

public class WorkspaceSO {
    private final UserSO user;
    private final String folder;
    private final String branch;
    private BooksSO books;
    
    public WorkspaceSO(UserSO user, String parentFolder, String branch) {
        this.user = user;
        this.folder = parentFolder + "/" + branch;
        this.branch = branch;
    }

    public UserSO getUser() {
        return user;
    }

    public String getFolder() {
        return folder;
    }

    public String getBranch() {
        return branch;
    }

    public DirAccess dao() {
        return user.dao();
    }

    public BooksSO getBooks() {
        if (books == null) {
            // late access auf books
            // Dies ist der erste richtige Zugriff auf den Workspace.
            // Git Repo pullen!
            dao().initWorkspace(this, false);
            books = new BooksSO(this);
            Logger.info("User " + user.getUser().getLogin() + " greift das erste Mal auf Workspace " + branch + " zu. Bücher: " + books.size());
        }
        return books;
    }

    public void pull() {
        pull(false);
    }
    
    public void pull(boolean forceClone) {
        dao().initWorkspace(this, forceClone);
        books = new BooksSO(this);
        Logger.info("User " + user.getUser().getLogin() + " has updated workspace " + branch + ". Books: " + books.size());
    }

    public List<SeiteSO> findTag(String tag) {
        String x = TagsSO.cleanTag(tag);
        List<SeiteSO> ret = new ArrayList<>();
        for (BookSO book : books) {
            ret.addAll(book.findTag(x));
        }
        return ret.stream().sorted((a, b) -> a.getSort().compareToIgnoreCase(b.getSort())).collect(Collectors.toList());
    }

    public TagNList getAllTags() {
        TagNList tags = new TagNList();
        books.forEach(book -> book.addAllTags(tags));
        return tags;
    }
}
