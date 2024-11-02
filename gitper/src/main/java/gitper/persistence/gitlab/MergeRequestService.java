package gitper.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestParams;

import gitper.User;
import gitper.base.StringService;

public class MergeRequestService {
    private boolean waitLonger = false;
    
    public void createAndSquash(String title, String branch, String targetBranch, String gitlabUrl,
            String project, User user) throws GitLabApiException {
        work(title, branch, targetBranch, gitlabUrl, project, user, Boolean.TRUE);
    }

    public void createAndMerge(String title, String branch, String targetBranch, String gitlabUrl,
            String project, User user) throws GitLabApiException {
        work(title, branch, targetBranch, gitlabUrl, project, user, Boolean.TRUE);
    }

    private void work(String title, String branch, String targetBranch, String gitlabUrl,
            String project, User user, Boolean squash) throws GitLabApiException {
        try (GitLabApi gitLabApi = GitFactory.getGitLabApi(user)) {
            MergeRequestParams params = new MergeRequestParams()
                    .withSourceBranch(branch)
                    .withTargetBranch(targetBranch)
                    .withTitle(StringService.isNullOrEmpty(title) ? ("Merge Request " + branch + " -> " + targetBranch) : title)
                    .withRemoveSourceBranch(Boolean.TRUE)
                    .withSquash(squash);
            
            MergeRequestApi api = gitLabApi.getMergeRequestApi();
            MergeRequest mr = api.createMergeRequest(project, params);
            waitForCanBeMerged(project, api, mr, targetBranch, user);
            
            api.acceptMergeRequest(project, mr.getIid());
            waitForMergedState(project, api, mr);
        }
    }
    
    private void waitForCanBeMerged(String project, MergeRequestApi api, MergeRequest mr, String targetBranch, User user) throws GitLabApiException {
        int loop = 0;
        int time = 500;
        while (true) {
            MergeRequest s = null;
            try {
                s = api.getMergeRequest(project, mr.getIid());
            } catch (Exception ignore) {
            }
            if (s != null) {
                if ("can_be_merged".equals(getMergeStatus(s))) {
                    break;
                } else if ("cannot_be_merged".equals(getMergeStatus(s))) {
                    throw new MergeRequestException(mr.getIid(), targetBranch, user.getGitlabConfig().getGitlabMergeRequestPath());
                }
            }
            if (++loop > (1000 / time) * 60) { // 1 minute
                throw new RuntimeException("Killer loop. Merge Reqest merge state does not become can_be_merged" +
                    " and it is: " + getMergeStatus(s) + " ID is " + mr.getIid() +
                    "  Please check Merge Request manually.");
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupt error while waiting for can_be_merged state", e);
            }
        }
    }

    private String getMergeStatus(MergeRequest s) {
        return s == null ? "" : s.getMergeStatus(); // Will be deprecated in 5.2.0. However, other method does not work.
    }

    private void waitForMergedState(String project, MergeRequestApi api, MergeRequest mr) throws GitLabApiException {
        int loop = 0;
        int time = 200;
        int max = (1000 / time) * 60; // 1 minute
        if (waitLonger) { // for migration (commit with many files)
            max *= 4;
        }
        while (true) {
            MergeRequest s = null;
            try {
                s = api.getMergeRequest(project, mr.getIid());
            } catch (Exception ignore) {
            }
            if (s != null && "merged".equals(s.getState())) {
                break;
            }
            if (++loop > max) {
                throw new RuntimeException("Killer loop while waiting for merged MR. ID: " + mr.getIid()
                    + "  Please check MR state manually. State is: " + s.getState() + ", merge state is: "
                    + getMergeStatus(s) + ", error: " + s.getMergeError());
            }
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupt error while waiting for merged state", e);
            }
        }
    }
    
    public void waitLonger() {
        waitLonger = true;
    }
}
