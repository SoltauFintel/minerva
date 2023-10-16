package minerva.model;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.access.DirAccess;
import minerva.base.FileService;
import minerva.base.MList;

public class WorkspacesSO extends MList<WorkspaceSO> {
    public static final String MINERVA_BRANCH = "minerva";
    private final String userFolder;

    public WorkspacesSO(UserSO user, String userFolder) {
        super(new WorkspacesComparator());
        this.userFolder = userFolder;
        DirAccess dao = user.dao();
        List<String> workspaceFolders = dao.getAllFolders(userFolder);
        if (workspaceFolders.isEmpty() || !workspaceFolders.contains("master")) {
            workspaceFolders.add(0, "master");
        }
        Logger.info(user.getLogin() + " | User has these workspaces: " + workspaceFolders);
        for (String branch : workspaceFolders) {
            add(new WorkspaceSO(user, userFolder, branch));
        }
    }

    public String getFolder() {
        return userFolder;
    }

    public WorkspaceSO byBranch(UserSO user, String branch) {
        if (isEmpty()) {
            throw new RuntimeException("There are no workspaces.");
        }
        for (WorkspaceSO w : this) {
            if (w.getBranch().equals(branch)) {
                return w;
            }
        }
        Logger.info(user.getLogin() + " | " + branch + " | Workspace not found. Workspace will be created.");
        return user.getWorkspaces().addWorkspace(branch, user);
    }

    @Override
    public boolean remove(WorkspaceSO workspace) {
        FileService.deleteFolder(new File(workspace.getFolder()));
        boolean ret = super.remove(workspace);
        UserSO user = workspace.getUser();
        if (user.getCurrentWorkspace() == workspace) {
            user.setCurrentWorkspace(user.masterWorkspace());
        }
        return ret;
    }

    public List<String> getAddableBranches(WorkspaceSO ref) {
        return MinervaWebapp.factory().getBackendService().getAddableBranches(this, ref);
    }

    public WorkspaceSO addWorkspace(String branch, UserSO user) {
        WorkspaceSO workspace = new WorkspaceSO(user, userFolder, branch);
        add(workspace);
        workspace.pull(true);
        return workspace;
    }

    public static class WorkspacesComparator implements Comparator<WorkspaceSO> {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public int compare(WorkspaceSO a, WorkspaceSO b) {
            return sort(a).compareToIgnoreCase(sort(b));
        }

        private String sort(WorkspaceSO w) {
            String branch = w.getBranch();
            String ret = map.get(branch);
            if (ret == null) {
                ret = make(branch);
                map.put(branch, ret);
            }
            return ret;
        }

        private String make(String branch) {
            if ("master".equals(branch)) {
                return "1master";
            } else {
                char f = branch.charAt(0);
                if (f >= '1' && f <= '9') {
                    String[] t = branch.split("\\.");
                    String neu = "";
                    for (int i = 0; i < t.length; i++) {
                        int zahl;
                        try {
                            zahl = Integer.parseInt(t[i]);
                            neu += new DecimalFormat("0000").format(100 - zahl) + ".";
                        } catch (NumberFormatException e) {
                            neu += t[i] + ".";
                        }
                    }
                    return "2" + neu;
                }
            }
            return "3" + branch;
        }
    }
}
