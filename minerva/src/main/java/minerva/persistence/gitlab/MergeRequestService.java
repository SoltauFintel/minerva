package minerva.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.MergeRequestApi;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.MergeRequestParams;

import minerva.base.StringService;

public class MergeRequestService {

    public void createAndSquashMergeRequest(String title, String branch, String targetBranch, String gitlabUrl, String project, String user, String password) throws GitLabApiException {
        try (GitLabApi gitLabApi = GitLabApi.oauth2Login(gitlabUrl, user, password)) {
            MergeRequestParams params = new MergeRequestParams()
                    .withSourceBranch(branch)
                    .withTargetBranch(targetBranch)
                    .withTitle(StringService.isNullOrEmpty(title) ? ("Merge Request " + branch + " -> " + targetBranch) : title)
                    .withRemoveSourceBranch(Boolean.TRUE)
                    .withSquash(Boolean.TRUE);
            
            MergeRequestApi api = gitLabApi.getMergeRequestApi();
            MergeRequest mr = api.createMergeRequest(project, params);
            waitForCanBeMerged(project, api, mr);
            
            api.acceptMergeRequest(project, mr.getIid());
            waitForMergedState(project, api, mr);
        }
    }
    
    private void waitForCanBeMerged(String project, MergeRequestApi api, MergeRequest mr) throws GitLabApiException {
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
                    throw new MergeRequestException("Merge Request " + mr.getIid() + " can not be merged!");
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
        while (true) {
            MergeRequest s = null;
            try {
                s = api.getMergeRequest(project, mr.getIid());
            } catch (Exception ignore) {
            }
            if (s != null && "merged".equals(s.getState())) {
                break;
            }
            if (++loop > (1000 / time) * 60) { // 1 minute
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
}
