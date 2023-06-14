package minerva.persistence.gitlab;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.User;
import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.rest.REST;
import github.soltaufintel.amalia.web.action.Action;
import github.soltaufintel.amalia.web.action.Escaper;
import github.soltaufintel.amalia.web.config.AppConfig;
import minerva.auth.LoginPage;
import minerva.model.GitFactory;

public class GitlabAuthCallbackAction extends Action {

    @Override
    protected void execute() {
        String code = ctx.queryParam("code");
        String state = ctx.queryParam("state");

        if (code == null || code.isEmpty()) {
            Logger.error("code is empty");
            ctx.redirect("/");
        } else if (GitlabAuthAction.knownStates.contains(state)) {
            // state ok
            AppConfig cfg = new AppConfig();
            String param = "client_id=" + u(cfg.get("gitlab-appid")) + //
                    "&client_secret=" + u(cfg.get("gitlab-secret")) + //
                    "&code=" + u(code) + //
                    "&grant_type=authorization_code&redirect_uri=" + u(cfg.get("gitlab-auth-callback"));

            Answer answer = new REST(cfg.get("gitlab.url") + "/oauth/token").post(param).fromJson(Answer.class);

            try (GitLabApi gitLabApi = GitFactory.initWithAccessToken(answer.getAccess_token())) {
                User currentUser = gitLabApi.getUserApi().getCurrentUser();
                String login = currentUser.getUsername();
                String mail = currentUser.getEmail();
                GitlabUser user = new GitlabUser(login, "");
                user.setMail(mail);
                user.setAccessToken(answer.getAccess_token());
                user.setRefreshToken(answer.getRefresh_token());
                LoginPage.login2(ctx, login, user);
                Logger.info("Login by OAuth2 access token ok. " + login + " <" + mail + ">");
                // kein Redirect, ich versteh nicht warum. Denn andernfalls tritt IllegalStateException auf.
            } catch (GitLabApiException e) {
                Logger.error(e);
                ctx.redirect("/");
            }
        } else {
            Logger.debug("ERROR. Wrong state: " + state + "\n" + GitlabAuthAction.knownStates);
            ctx.redirect("/");
        }
    }

    private String u(String k) {
        return Escaper.urlEncode(k, "");
    }
}
