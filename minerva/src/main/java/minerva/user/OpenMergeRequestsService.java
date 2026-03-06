package minerva.user;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.mail.Mail;
import github.soltaufintel.amalia.timer.AbstractTimer;
import gitper.base.StringService;
import gitper.persistence.gitlab.UserMergeRequests;
import minerva.MinervaWebapp;
import minerva.config.MinervaConfig;
import minerva.model.StatesSO;
import minerva.model.UserSO;

public class OpenMergeRequestsService {
    
    /**
     * Einmal täglich schauen, ob es offene Merge Requests gibt.
     */
    public static class OpenMergeRequestsTimer extends AbstractTimer {

        @Override
        protected void timerEvent() {
            Logger.info("OpenMergeRequestsTimer");
            new OpenMergeRequestsService().process(StatesSO.login());
        }
    }
    
    public static class OpenMergeRequestsAction extends UAction {

        @Override
        protected void execute() {
            Logger.info(user.getLogin() + " | OpenMergeRequestsAction");
            new OpenMergeRequestsService().process(user);
        }
    }
    
    void process(UserSO pUser) {
        var workspace = pUser.masterWorkspace();
        List<UserMergeRequests> mr = workspace.dao().areThereOpenMergeRequests(workspace);
        if (mr.isEmpty()) {
            return;
        }
        String msg = "[OpenMergeRequestsTimer] " + mr;
        Logger.info(msg);
        workspace.getUser().log(msg);

        var mailAddresses = getAdminMailAddresses();
        if (mailAddresses.isEmpty()) {
            Logger.info("[OpenMergeRequestsTimer] no admin mail addresses -> do nothing");
            return;
        }
        Logger.info("[OpenMergeRequestsTimer] admin mailAddresses: " + mailAddresses); // XXX debug
        
        if (!isDirty(mr)) {
            return; // abort if there are no changes. We don't want every day the same mail based on same data.
        }

        MinervaConfig config = MinervaWebapp.factory().getConfig();
        Mail mail = getMail(mr, config);
        for (String ma : mailAddresses) {
            mail.setToEmailaddress(ma);
            config.sendMail(mail);
        }
    }

    private Set<String> getAdminMailAddresses() {
        var admins = MinervaWebapp.factory().getAdmins();
        return UserAccess.loadUsers().stream()
                .filter(u -> admins.contains(u.getLogin()) && !StringService.isNullOrEmpty(u.getMailAddress()))
                .map(u -> u.getMailAddress())
                .collect(Collectors.toSet());
    }
    
    private boolean isDirty(List<UserMergeRequests> mrList) {
        boolean dirty = false;
        for (User user : UserAccess.loadUsers()) {
            for (UserMergeRequests mr : mrList) {
                if (mr.getName().equals(user.getRealName())
                        && (user.getOmrIdList() == null || !user.getOmrIdList().equals(mr.getIdList()))) {
                    dirty = true;
                    user.setOmrIdList(mr.getIdList());
                    UserAccess.saveUser(user);
                }
            }
        }
        return dirty;
    }

    private Mail getMail(List<UserMergeRequests> mr, MinervaConfig config) {
        var gitlabUrl = config.getGitlabUrl() + "/" + config.getGitlabProject() + config.getGitlabMergeRequestPath();
        Mail mail = new Mail();
        mail.setSubject("Open merge requests");
        mail.setBody("Diese Benutzer haben offene Merge Requests im Gitlab: "
                + mr.stream().map(i -> i.getName()).collect(Collectors.joining(", "))
                + "\nSolche Merge Requests stellen ungespeicherte Änderungen dar. " + gitlabUrl);
        Logger.info("[OpenMergeRequestsTimer] body: " + mail.getBody()); // XXX debug
        return mail;
    }
}
