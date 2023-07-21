package minerva.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.google.common.base.Strings;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.seite.link.InvalidLinksModel;
import minerva.user.User;

public class UserSO {
    private final User user;
    private final WorkspacesSO workspaces;
    private WorkspaceSO currentWorkspace;
    private DirAccess dao;
    private LocalDateTime lastAction;
    private InvalidLinksModel linksModel;
    private SeitenSO orderPagesModel; // SeitenSO working copy
    /** branch list */
    private final Set<String> hasToPull = new HashSet<>();
    
    public UserSO(User user) {
        this.user = user;
        String userFolder = getWorkspacesFolder() + "/" + user.getFolder();
        dao = MinervaWebapp.factory().getDirAccess(this);
        this.workspaces = new WorkspacesSO(this, userFolder);
    }

    public User getUser() {
        return user;
    }
    
    public String getLogin() {
        return user.getLogin();
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
        UserSettingsSO us = getUserSettings();
        List<String> favorites = us.getFavorites();
        if (favorites.contains(id)) {
            favorites.remove(id);
        } else {
            favorites.add(id);
        }
        us.save();
    }

    public void toggleWatch(String id) {
        UserSettingsSO us = getUserSettings();
        List<String> watchlist = us.getWatchlist();
        if (watchlist.contains(id)) {
            watchlist.remove(id);
        } else {
            watchlist.add(id);
        }
        us.save();
    }

    public List<String> getFavorites() {
        return getUserSettings().getFavorites();
    }
    
    public UserSettingsSO getUserSettings() {
        return UserSettingsSO.load(user.getLogin());
    }
    
    public void onlyAdmin() {
        if (!MinervaWebapp.factory().getAdmins().contains(user.getLogin())) {
            throw new RuntimeException("User " + user.getLogin() + " is not an admin!");
        }
    }

    public void onlyWithExportRight() {
        if (!MinervaWebapp.factory().getPersonsWithExportRight().contains(user.getLogin())) {
            throw new RuntimeException("User " + user.getLogin() + " has no export right!");
        }
    }

    public void log(String msg) {
        try (FileWriter w = new FileWriter(getServerlogFile(), true)) {
            w.write(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ")));
            w.write(Strings.padEnd(user.getLogin(), 16, ' '));
            w.write(" ");
            w.write(msg);
            w.write("\n");
        } catch (IOException e) {
            Logger.error(e);
        }
    }
    
    public String getServerlog() {
        return FileService.loadPlainTextFile(getServerlogFile());
    }
    
    private File getServerlogFile() {
        return new File(getWorkspacesFolder() + "/server.log");
    }

    private String getWorkspacesFolder() {
        return MinervaWebapp.factory().getConfig().getWorkspacesFolder();
    }

    public boolean popHasToPull(String branch) {
        if (hasToPull.contains(branch)) {
            hasToPull.remove(branch);
            return true;
        }
        return false;
    }

    public void addHasToPull(String branch) {
        hasToPull.add(branch);
    }
}
