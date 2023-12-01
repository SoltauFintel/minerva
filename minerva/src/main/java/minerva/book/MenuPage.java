package minerva.book;

import static minerva.base.StringService.umlaute;

import com.github.template72.data.DataCondition;
import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.MinervaWebapp;
import minerva.config.MinervaFactory;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;
import minerva.user.UserAccess;
import minerva.workspace.WPage;

public class MenuPage extends WPage {

	@Override
	protected void execute() {
		
		header(n("Menu"));
		workspaces();
		menu();
		favorites();
	}

	private void menu() {
		MinervaFactory fac = MinervaWebapp.factory();
		boolean isAdmin = "1".equals(ctx.req.session().attribute("admin"));
		DataList list = list("commands");
//        if (MinervaWebapp.factory().isGitlab()) {
//        	menu(list, "Workspaces", "fa-folder-open-o", "/");
//        }
		menu(list, "myTasks", "fa-inbox", "/w/:branch/my-tasks");
		menu(list, "preview", "fa-thumbs-o-up bluebook", "/p/:branch");
		menu(list, "formulaEditor", "fa-superscript", "/math");
		if (fac.isGitlab()) {
			menu(list, "workspaceHistory", "fa-clock-o", "/w/:branch/history");
		}
		menu(list, "tagCloud", "fa-cloud", "/w/:branch/tag-cloud?m=n");
		menu(list, "allHelpKeys", "fa-question-circle", "/w/:branch/help-keys");
		if (fac.isCustomerVersion()) {
			menu(list, "Broken Mappings", "fa-chain-broken", "/w/:branch/broken-mappings");
		}
		export(list);
		workspace(list);
		additionalMenuItems(list);
		onlinehelp(fac, list);
		admin(fac, isAdmin, list);
	}

	private void workspace(DataList list) {
		menu(list, "pullWS", "fa-refresh", "/w/:branch/pull");
		menu(list, "cloneWS", "fa-refresh red", "/w/:branch/pull?force=1");
    	menu(list, "createWS", "fa-folder", "/create-workspace");
		menu(list, "deleteWS", "fa-trash-o red", "/w/:branch/delete");
		menu(list, "createBranch", "fa-code-fork", "/branch/:branch");
		menu(list, "mergeBranch", "fa-code-fork", "/merge/:branch");
		if (user.getUser().getDelayedPush().contains(branch)) {
			menu(list, "endFSMode", "fa-flag-checkered fsmode", "/w/:branch/deactivate-f-s-mode");
		} else {
			menu(list, "beginFSMode", "fa-flag-checkered", "/w/:branch/activate-f-s-mode");
		}
	}

	protected void additionalMenuItems(DataList list) { // template method
	}

	private void onlinehelp(MinervaFactory fac, DataList list) {
		if (fac.isCustomerVersion()
		        && !fac.isGitlab()
		        && !fac.getConfig().getSubscribers().isEmpty()) {
			menu(list, "updateOnlineHelp", "fa-upload", "/w/:branch/push-data");
		}
	}

	private void export(DataList list) {
		menu(list, "export", "fa-upload", "/w/:branch/export");
		if (UserAccess.hasExportRight(user.getLogin())) {
			menu(list, "exportTemplates", "fa-file-text-o", "/ets/:branch");
		}
	}

	private void admin(MinervaFactory fac, boolean isAdmin, DataList list) {
		if (!MinervaWebapp.factory().getAdmins().contains(user.getLogin())) {
			return;
		}
		if (isAdmin) {
			menu(list, "dropAdminRights", "fa-trophy", "/activate-admin-rights?m=0");
			menu(list, "books", "fa-book", "/w/:branch");
			if (fac.isGitlab()) {
				menu(list, "exclusions", "fa-bank", "/w/:branch/exclusions/edit");
			}
			menu(list, "manageUsers", "fa-users", "/users");
			menu(list, "reindex", "fa-refresh", "/w/:branch/index");
			menu(list, "serverlog", "fa-paw", "/serverlog");
			if (isMigrationAllowed()) {
				menu(list, "Confluence Import", "fa-cloud-download", "/migration/:branch");
			}
		} else {
			menu(list, "giveAdminRights", "fa-trophy", "/activate-admin-rights");
		}
	}
	
    protected void menu(DataList list, String text, String icon, String link) {
    	DataMap map = list.add();
    	map.put("text", esc(n(text)));
    	map.put("icon", esc(icon));
    	map.put("link", esc(link.replace(":branch", esc(branch))));
    	map.put("sep", "-".equals(text));
    }

    private boolean isMigrationAllowed() {
        if ("1".equals(MinervaWebapp.factory().getConfig().getMigration())) {
            String migrationUsers = MinervaWebapp.factory().getConfig().getMigrationUsers();
            if (migrationUsers.isEmpty()) {
                return true;
            }
            for (String user : migrationUsers.split(",")) {
                if (this.user.getLogin().equals(user.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

	private void workspaces() {
		DataList list = list("workspaces");
        if (MinervaWebapp.factory().isGitlab()) {
	        for (WorkspaceSO workspace : user.getWorkspaces()) {
	            DataMap map = list.add();
	            map.put("link", workspace.getBooks().isEmpty() ? "" :
	            	("/b/" + esc(workspace.getBranch()) + "/" + esc(workspace.getBooks().get(0).getBook().getFolder())));
	            map.put("link", workspace.getBooks().isEmpty() ? "" : ("/w/" + esc(workspace.getBranch()) + "/menu"));
	            if (branch.equals(workspace.getBranch())) {
	            	map.put("text", esc(workspace.getBranch()));
	            	map.put("icon", "fa-folder-open-o current-workspace");
	            } else {
	            	map.put("text", esc(workspace.getBranch()));
	            	map.put("icon", "fa-folder-open-o");
	            }
	        }
        }
	}

	private void favorites() {
		boolean hasFavorites = ((DataCondition) model.get("hasLastEditedPage")).isTrue();
		DataList list = list("favorites");
        String linkPrefix = "/s/" + branch + "/";
        for (String id : user.getFavorites()) {
            for (BookSO book : user.getWorkspace(branch).getBooks()) {
                SeiteSO seite = book._seiteById(id);
                if (seite != null) {
                    DataMap map = list.add();
                    map.put("link", esc(linkPrefix + book.getBook().getFolder() + "/" + seite.getId()));
                    map.put("title", esc(seite.getTitle()));
                    hasFavorites = true;
                }
            }
        }
        list.sort((a, b) -> umlaute(a.get("title").toString()).compareTo(umlaute(b.get("title").toString())));
        put("hasFavorites", hasFavorites);
	}
}
