package minerva.persistence.gitlab;

import java.io.File;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.pmw.tinylog.Logger;

public class UpToDateCheckService {

    public boolean areThereRemoteUpdates(File workspace, String targetBranch, String user, String password) {
        try (Git git = Git.open(workspace)) {
            FetchResult f = git.fetch()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(user, password))
                    .call();
            long n = f.getTrackingRefUpdates().stream().filter(i -> i.getRemoteName().endsWith("/" + targetBranch)).count();
            return n > 0;
        } catch (Exception e) {
            Logger.error(e);
            return true;
        }
    }
}
