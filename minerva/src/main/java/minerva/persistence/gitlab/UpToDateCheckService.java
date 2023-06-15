package minerva.persistence.gitlab;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.FetchResult;
import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.model.GitFactory;
import minerva.model.WorkspaceSO;

public class UpToDateCheckService {

    public boolean areThereRemoteUpdates(File workspace, String targetBranch, GitlabUser user) {
        try (Git git = Git.open(workspace)) {
            FetchResult f = git.fetch()
                    .setCredentialsProvider(GitFactory.getUsernamePasswordCredentialsProvider(user))
                    .call();
            long n = f.getTrackingRefUpdates().stream().filter(i -> i.getRemoteName().endsWith("/" + targetBranch)).count();
            return n > 0;
        } catch (Exception e) {
            Logger.error(e);
            return true;
        }
    }
    
    public static void check(WorkspaceSO workspace, UpdateAction updateAction) {
        if (!MinervaWebapp.factory().isGitlab()) {
            return;
        }
        File workspaceFolder = new File(workspace.getFolder());
        GitlabUser gitlabUser = (GitlabUser) workspace.getUser().getUser();
        boolean areThereRemoteUpdates = new UpToDateCheckService().areThereRemoteUpdates(workspaceFolder,
                workspace.getBranch(), gitlabUser);
        if (areThereRemoteUpdates) {
            workspace.pull();
            updateAction.update();
        }
    }
    
    public interface UpdateAction {
        
        /**
         * Execute some code after workspace has been pulled.
         */
        void update();
    }
}
