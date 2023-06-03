package minerva.persistence.gitlab;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.FetchResult;
import org.pmw.tinylog.Logger;

import minerva.model.GitFactory;

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
}
