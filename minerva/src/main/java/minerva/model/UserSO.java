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
import java.util.function.Predicate;

import org.pmw.tinylog.Logger;

import com.google.common.base.Strings;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.spark.Context;
import gitper.access.DirAccess;
import gitper.base.StringService;
import minerva.MinervaWebapp;
import minerva.base.Tosmap;
import minerva.export.ExportUserSettings;
import minerva.seite.CommentWithSeite;
import minerva.seite.link.InvalidLinksModel;
import minerva.task.TaskPriority;
import minerva.user.CustomerMode;
import minerva.user.User;
import minerva.user.UserAccess;
import minerva.user.quickbuttons.Quickbutton;

public class UserSO {
    private User user;
    private final String folder;
    private final JournalSO journal;
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
    public static LoginRoutine loginRoutine = userSO -> {};
    
    public UserSO(User user) {
        if (user == null) {
            throw new RuntimeException("user must not be null");
        }
        UserAccess.validateLogin(user.getLogin());
        this.user = user;
        folder = MinervaWebapp.factory().getBackendService().getUserFolder(user);
        dao = MinervaWebapp.factory().getBackendService().getDirAccess();
        journal = new JournalSO(this);
        workspaces = new WorkspacesSO(this, getWorkspacesFolder() + "/" + folder);
        loginRoutine.login(this);
    }

    public User getUser() {
        return user;
    }
    
    public String getLogin() {
        return user.getLogin();
    }
    
    public String getUserFolder() {
        return getWorkspacesFolder() + "/" + folder;
    }
    
    public JournalSO getJournal() {
        return journal;
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
    
    public void selectPageLanguage(String lang) {
        load();
        user.setPageLanguage(lang);
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
    
    public void toggleShowAllPages() {
        load();
        user.setShowAllPages(!user.isShowAllPages());
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
    
    public void setDatabase(String database) {
        load();
        user.setDatabase(database);
        save();
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
            long endtime = System.currentTimeMillis() + 1000 * 60 * 60; // 60 minutes
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
            String endTime = (String) Tosmap.get(key);
            if (endTime != null) {
                return new LoginAndEndTime(key.substring(key.lastIndexOf("/") + 1), endTime);
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

    public List<CommentWithSeite> getUndoneCommentsToBeCompletedByMe(String branch, String login) {
        if (StringService.isNullOrEmpty(login)) {
            login = user.getLogin();
        }
        final String x = login;
        return comments(branch, login, cws -> !cws.getComment().isDone() && x.equals(cws.getComment().getPerson()));
    }

    public List<CommentWithSeite> getUndoneCommentsCreatedByMe(String branch, String login) {
        if (StringService.isNullOrEmpty(login)) {
            login = user.getLogin();
        }
        final String x = login;
        return comments(branch, login, cws -> !cws.getComment().isDone() // undone
                && !StringService.isNullOrEmpty(cws.getComment().getPerson()) // has person
                && !x.equals(cws.getComment().getPerson()) // not for myself
                && x.equals(cws.getComment().getUser())); // created by me
    }

    private List<CommentWithSeite> comments(String branch, String login, Predicate<CommentWithSeite> filter) {
        List<CommentWithSeite> cwsList = new ArrayList<>();
        for (BookSO book : getWorkspace(branch).getBooks()) {
            for (CommentWithSeite n : book.getSeiten().getAllComments()) {
                if (filter.test(n)) {
                    cwsList.add(n);
                }
            }
        }
        return cwsList;
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
    
    public String getAttachmentCategory() {
        load();
        String ret = user.getAttachmentCategory();
        return StringService.isNullOrEmpty(ret) ? "datei" : ret;
    }
    
    public void saveAttachmentCategory(String category) {
        load();
        user.setAttachmentCategory(category);
        save();
    }
    
    public void saveReleaseNumber(String rn) {
        load();
        user.setPublishReleaseNumber(rn == null ? "" : rn.trim());
        save();
    }
    
    public void saveCustomerMode(String customer) {
        load();
        user.setCustomerMode(customer);
        save();
    }
    
    public CustomerMode getCustomerMode() {
        return new CustomerMode(user.getCustomerMode());
    }
    
    public static boolean isAdmin(Context ctx) {
        return "1".equals(ctx.req.session().attribute("admin"));
    }
    
    public interface LoginRoutine {
        void login(UserSO userSO);
    }
    
    public Quickbutton addQuickbutton(String label, String link) {
        Quickbutton qb = new Quickbutton();
        qb.setLabel(label);
        qb.setLink(link);
        load();
        List<Quickbutton> quickbuttons = user.getQuickbuttons();
        if (quickbuttons.size() >= 50) {
            throw new RuntimeException("Too many quick buttons!");
        }
        quickbuttons.removeIf(i -> i.getLink().equals(link));
        quickbuttons.add(qb);
        save();
        return qb;
    }
    
    public void saveQuickbuttons() {
        save();
    }

    public void toggleQuickbuttons() {
        load();
        user.setShowQuickbuttons(!user.isShowQuickbuttons());
        save();
    }
    
    public List<Quickbutton> getQuickbuttonsFromOtherUsers() {
        List<Quickbutton> ret = new ArrayList<>();
        for (User u : UserAccess.loadUsers()) {
            for (Quickbutton b : u.getQuickbuttons()) {
                if (!b.isOnlyMe() && !"/q/config".equals(b.getLink())) {
                    boolean found = false;
                    for (Quickbutton x : ret) {
                        if (x.getLink().equals(b.getLink())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ret.add(b);
                    }
                }
            }
        }
        ret.sort((a, b) -> a.getLabel().compareToIgnoreCase(b.getLabel()));
        for (Quickbutton b : user.getQuickbuttons()) {
            ret.removeIf(i -> i.getLink().equals(b.getLink()));
        }
        return ret;
    }
    
    public String getCustomerRights() {
        load();
        return user.getCustomerRights();
    }
    
    public String getRolloutConfig() {
        load();
        return user.getRolloutConfig();
    }
    
    public void setRolloutConfig(String rolloutConfig) {
        load();
        user.setRolloutConfig(rolloutConfig);
        save();
    }
    
    public String getRolloutCustomer() {
        load();
        return user.getRolloutCustomer();
    }
    
    public void setRolloutCustomer(String rolloutCustomer) {
        load();
        user.setRolloutCustomer(rolloutCustomer);
        save();
    }
    
    public String getTargetBranch() {
        load();
        return user.getTargetBranch();
    }
    
    public void setTargetBranch(String tb) {
        load();
        user.setTargetBranch(tb);
        save();
    }

    public boolean isColoredHeadings() {
        load();
        return user.isColoredHeadings();
    }

    public void toggleColoredHeadings() {
        load();
        user.setColoredHeadings(!user.isColoredHeadings());
        save();
    }
    
    public List<String> getIgnoredBrokenLinks() {
        load();
        return user.getIgnoredBrokenLinks();
    }
    
    public void toggleIgnoredBrokenLink(String id) {
        load();
        var i = user.getIgnoredBrokenLinks();
        if ("CLEAR".equals(id)) { // alle wieder einblenden
            i.clear();
        } else {
            if (i.contains(id)) {
                i.remove(id);
            } else {
                i.add(id);
            }
        }
        save();
    }
}
