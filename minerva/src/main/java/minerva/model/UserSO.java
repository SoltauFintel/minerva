package minerva.model;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.user.User;

public class UserSO {
    private final User user;
    private final WorkspacesSO workspaces;
    private WorkspaceSO currentWorkspace;
    private DirAccess dao;

    public UserSO(User user) {
        this.user = user;
        String userFolder = MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/" + user.getFolder();
        dao = MinervaWebapp.factory().getDirAccess(this);
        this.workspaces = new WorkspacesSO(this, userFolder);
    }

    public User getUser() {
        return user;
    }

    public WorkspacesSO getWorkspaces() {
        return workspaces;
    }
    
    public WorkspaceSO getWorkspace(String branch) {
        return workspaces.byBranch(branch);
    }
    
    public String getLanguage() {
        return user.getLanguage();
    }

    public WorkspaceSO getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(WorkspaceSO currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }
    
    public String createSeite(String branch, String bookFolder, String parentSeiteId) {
        BookSO book = getWorkspace(branch).getBooks().byFolder(bookFolder);
        if (SeiteSO.ROOT_ID.equals(parentSeiteId)) {
            return book.createTopLevelSeite();
        }
        SeiteSO parentSeite = book.getSeiten().byId(parentSeiteId);
        return parentSeite.getSeiten().createSeite(parentSeite, book);
    }
    
    public DirAccess dao() {
        return dao;
    }
}
