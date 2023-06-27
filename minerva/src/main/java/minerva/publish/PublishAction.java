package minerva.publish;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.persistence.gitlab.GitlabUser;
import spark.Request;
import spark.Response;
import spark.Route;

public class PublishAction implements Route {

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String branch = req.queryParams("branch");
        String login = req.queryParams("login");
        String password = req.queryParams("password");
        String langsStr = req.queryParams("lang"); // should be "de,en"

        List<String> langs = new ArrayList<>();
        for (String lang: langsStr.split(",")) {
            langs.add(lang);
        }
        UserSO userSO = new UserSO(new GitlabUser(login, password));
        WorkspaceSO workspace = userSO.getWorkspace(branch);
        File targetFolder = new File(System.getenv("MINERVA_PUBLISHFOLDER"));

        PublishService ps = new PublishService(targetFolder, langs);
        ps.publish(workspace);
        Path zipFile = ps.zip();

        res.type("application/zip");
        res.header("Content-Disposition", "attachment; filename=\"publish.zip\"");
        res.raw().getOutputStream().write(Files.readAllBytes(zipFile));

        Logger.info("PublishAction finished");
        return res.raw();
    }
}
