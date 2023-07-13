package minerva.model;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.seite.link.InvalidLinksModel;
import minerva.user.User;
import minerva.user.UserSettings;

public class UserSO {
    private final User user;
    private final WorkspacesSO workspaces;
    private WorkspaceSO currentWorkspace;
    private DirAccess dao;
    private LocalDateTime lastAction;
    private InvalidLinksModel linksModel;
    private SeitenSO orderPagesModel; // SeitenSO working copy
    
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
    
    public String getGuiLanguage() {
        return user.getGuiLanguage();
    }

    public String getPageLanguage() {
        return user.getPageLanguage();
    }

    public WorkspaceSO getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(WorkspaceSO currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }
    
    /**
     * @param branch -
     * @param bookFolder -
     * @param parentSeiteId -
     * @return page ID
     */
    public String createSeite(String branch, String bookFolder, String parentSeiteId) {
        String id;
        BookSO book = getWorkspace(branch).getBooks().byFolder(bookFolder);
        if (SeiteSO.ROOT_ID.equals(parentSeiteId)) {
            id = book.createTopLevelSeite();
        } else {
            SeiteSO parentSeite = book.getSeiten().byId(parentSeiteId);
            id = parentSeite.getSeiten().createSeite(parentSeite, book, dao);
        }
        return id;
    }
    
    public DirAccess dao() {
        return dao;
    }

    public LocalDateTime getLastAction() {
        return lastAction;
    }

    public void setLastAction(LocalDateTime lastAction) {
        this.lastAction = lastAction;
    }

    public InvalidLinksModel getLinksModel() {
        return linksModel;
    }

    public void setLinksModel(InvalidLinksModel linksModel) {
        this.linksModel = linksModel;
    }

    public SeitenSO getOrderPagesModel() {
        return orderPagesModel;
    }

    public void setOrderPagesModel(SeitenSO orderPagesModel) {
        this.orderPagesModel = orderPagesModel;
    }

    public void toggleFavorite(String id) {
        UserSettings us = loadUserSettings();
        List<String> favorites = us.getFavorites();
        if (favorites.contains(id)) {
            favorites.remove(id);
        } else {
            favorites.add(id);
        }
        saveUserSettings(us);
    }
    
    public List<String> getFavorites() {
        return loadUserSettings().getFavorites();
    }
    
    public UserSettings loadUserSettings() {
        File file = getUserSettingsFile();
        if (file.isFile()) {
            return FileService.loadJsonFile(file, UserSettings.class);
        }
        return new UserSettings();
    }
    
    public void saveUserSettings(UserSettings us) {
        FileService.saveJsonFile(getUserSettingsFile(), us);
    }
    
    private File getUserSettingsFile() {
        return new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder() + "/" + user.getLogin()
                + "/user-settings.json");
    }
    
    public void onlyAdmin() {
        if (!MinervaWebapp.factory().getAdmins().contains(user.getLogin())) {
            throw new RuntimeException("User " + user.getLogin() + " is not an admin!");
        }
    }
}
