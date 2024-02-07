package minerva.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.google.common.base.Strings;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.StringService;
import minerva.base.Tosmap;
import minerva.export.ExportUserSettings;
import minerva.seite.link.InvalidLinksModel;
import minerva.seite.note.NoteWithSeite;
import minerva.task.TaskPriority;
import minerva.user.User;
import minerva.user.UserAccess;

public class UserSO {
    private User user;
    private final String folder;
    private final WorkspacesSO workspaces;
    private WorkspaceSO currentWorkspace;
    private DirAccess dao;
    private LocalDateTime lastAction;
    private InvalidLinksModel linksModel;
    private SeitenSO orderPagesModel; // SeitenSO working copy
    /** branch list */
    private final Set<String> hasToPull = new HashSet<>();
    private String lastSelectedBranch;
    private Map<String, TaskPriority> taskPriorities;
    
    public UserSO(User user) {
        if (user == null) {
            throw new RuntimeException("user must not be null");
        }
        UserAccess.validateLogin(user.getLogin());
        this.user = user;
        folder = MinervaWebapp.factory().getBackendService().getUserFolder(user);
        dao = MinervaWebapp.factory().getBackendService().getDirAccess();
        this.workspaces = new WorkspacesSO(this, getWorkspacesFolder() + "/" + folder);
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
        return workspaces.byBranch(this, branch);
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
        String name = currentWorkspace.getBranch();
        if (!"master".equals(name) && !"migration".equals(name)) {
            lastSelectedBranch = name;
        }
        this.currentWorkspace = currentWorkspace;
    }
    
    public String getLastSelectedBranch() {
        return lastSelectedBranch;
    }

    public void setLastSelectedBranch(String lastSelectedBranch) {
        this.lastSelectedBranch = lastSelectedBranch;
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
            SeiteSO parentSeite = book.seiteById(parentSeiteId);
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
    
    public void selectLanguage(String language, boolean pageMode) {
        if (!MinervaWebapp.factory().getLanguages().contains(language)) {
            throw new IllegalArgumentException("Illegal language value!");
        }
        load();
        user.setPageLanguage(language);
        if (!pageMode) {
            user.setGuiLanguage(language);
        }
        save();
    }

	public void toggleGuiLanguage() {
		load();
		String lang = user.getGuiLanguage();
		if ("en".equals(lang)) {
			lang = "de";
		} else {
			lang = "en";
		}
		user.setGuiLanguage(lang);
		save();
	}

    public void toggleFavorite(String id) {
        load();
        List<String> favorites = user.getFavorites();
        if (favorites.contains(id)) {
            favorites.remove(id);
        } else {
            favorites.add(id);
        }
        save();
    }

    public void toggleWatch(String id) {
        load();
        List<String> watchlist = user.getWatchlist();
        if (watchlist.contains(id)) {
            watchlist.remove(id);
        } else {
            watchlist.add(id);
        }
        save();
    }
    
    public void setLastEditedPage(String id) {
        load();
        user.setLastEditedPage(id);
        save();
    }

    public List<String> getFavorites() {
        return user.getFavorites();
    }
    
    public void saveExportSettings(String item, String customer, String lang, String format, String template, boolean cover, boolean toc, boolean chapters) {
        load();
        if (user.getExport() == null) {
            user.setExport(new ExportUserSettings());
        }
        ExportUserSettings ex = user.getExport();
		ex.setItem(item);
        ex.setCustomer(customer);
        ex.setLang(lang);
        ex.setFormat(format);
        ex.setTemplate(template);
        ex.setCover(cover);
        ex.setToc(toc);
        ex.setChapters(chapters);
        save();
    }
    
    public void onlyAdmin() {
        if (!MinervaWebapp.factory().getAdmins().contains(user.getLogin())) {
            throw new RuntimeException("User " + user.getLogin() + " is not an admin!");
        }
    }

    public void onlyWithExportRight() {
    	if (!UserAccess.hasExportRight(user.getLogin())) {
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
    
    public void onEditing(String otherUser, String branch, String seiteId, boolean finished) {
        String key = "editing:" + branch + "/" + seiteId + "/" + otherUser;
        if (finished) {
            Tosmap.remove(key); // remove soft page lock
        } else {
            long endtime = System.currentTimeMillis() + 1000 * 60 * 60;
            LocalDateTime enddate = Instant.ofEpochMilli(endtime).atZone(ZoneId.systemDefault()).toLocalDateTime();
            Tosmap.add(key, endtime, enddate.format(DateTimeFormatter.ofPattern("HH:mm"))); // soft page lock for 60 minutes
        }
    }
    
    public void finishMyEditings() {
        for (String key : Tosmap.search("editing:")) {
            if (key.endsWith("/" + getLogin())) {
                Tosmap.remove(key);
            }
        }
    }

    /**
     * @param branch -
     * @param seiteId -
     * @return LoginAndEndTime - or null if nobody is editing the page
     */
    public LoginAndEndTime hasEditingStarted(String branch, String seiteId) {
        String keyBegin = "editing:" + branch + "/" + seiteId + "/";
        List<String> keys = Tosmap.search(keyBegin);
        for (String key : keys) {
            if (!key.endsWith("/" + user.getLogin())) { // it's not me
                String endTime = (String) Tosmap.get(key);
                if (endTime != null) {
                    return new LoginAndEndTime(key.substring(key.lastIndexOf("/") + 1), endTime);
                }
            }
        }
        return null;
    }

    public static class LoginAndEndTime {
        /** locked by user */
        private final String login;
        /** locked until */
        private final String endTime;

        public LoginAndEndTime(String login, String endTime) {
            this.login = login;
            this.endTime = endTime;
        }

        public String getLogin() {
            return login;
        }

        public String getEndTime() {
            return endTime;
        }
    }

    public List<NoteWithSeite> getNotes(String branch, String login) {
    	if (StringService.isNullOrEmpty(login)) {
    		login = user.getLogin();
    	}
        List<NoteWithSeite> notes = new ArrayList<>();
        for (BookSO book : getWorkspace(branch).getBooks()) {
            for (NoteWithSeite n : book.getSeiten().getAllNotes()) {
                if (!n.getNote().isDone() && login.equals(n.getNote().getPerson())) {
                    notes.add(n);
                }
            }
        }
        return notes;
    }
    
    public void activateDelayedPush(String branch) {
        load();
        if (!user.getDelayedPush().contains(branch)) {
            user.getDelayedPush().add(branch);
            save();
            Logger.info(getLogin() + " | " + branch + " | file-system mode activated");
        }
    }

    public void deactivateDelayedPush(String branch) {
        load();
        if (!user.getDelayedPush().contains(branch)) {
            throw new RuntimeException("Can only be called for active file-system mode.");
        }
        user.getDelayedPush().remove(branch);
        save();
        Logger.info(getLogin() + " | " + branch + " | file-system mode deactivated");
    }

    public boolean isDelayedPush(String branch) {
        load();
        return user.getDelayedPush().contains(branch);
    }
    
    public User getFreshUser() {
        load();
        return user;
    }

    private void load() {
        User u = UserAccess.loadUser(user.getLogin());
        if (u == null) {
            Logger.info("create User " + user.getLogin());
            save();
        } else {
            user = u;
        }
    }

    private void save() {
        UserAccess.saveUser(user);
    }
    
    public WorkspaceSO masterWorkspace() {
        return getWorkspaces().byBranch(this, "master");
    }
    
    public void setTaskPriority(String taskId, TaskPriority priority) {
        if (taskPriorities == null) {
            taskPriorities = loadTaskPriorities();
        }
        taskPriorities.put(taskId, priority);
        saveTaskPriorities(taskPriorities);
    }
    
    public TaskPriority getTaskPriority(String taskId) {
        if (taskPriorities == null) {
            taskPriorities = loadTaskPriorities();
        }
        TaskPriority ret = taskPriorities.get(taskId);
        return ret == null ? TaskPriority.NORMAL : ret;
    }
    
    private Map<String, TaskPriority> loadTaskPriorities() {
        Map<String, TaskPriority> map = new HashMap<>();
        load();
        if (user.getTaskPriorities() != null) {
            for (String line : user.getTaskPriorities()) {
                int o = line.indexOf("=");
                map.put(line.substring(0, o), TaskPriority.valueOf(line.substring(o + 1)));
            }
        }
        return map;
    }
    
    private void saveTaskPriorities(Map<String, TaskPriority> map) {
        List<String> list = new ArrayList<>();
        for (Entry<String, TaskPriority> e : map.entrySet()) {
            list.add(e.getKey() + "=" + e.getValue().name());
        }
        load();
        user.setTaskPriorities(list);
        save();
    }
}
