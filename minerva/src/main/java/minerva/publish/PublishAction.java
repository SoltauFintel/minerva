package minerva.publish;

import static minerva.base.StringService.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Action;
import minerva.base.FileService;
import minerva.model.UserSO;
import minerva.model.WorkspaceSO;
import minerva.persistence.gitlab.GitlabUser;

public class PublishAction extends Action {
    private static String handle = "handle";

    @Override
    protected void execute() {
        String branch = ctx.queryParam("branch");
        String login = ctx.queryParam("login");
        String password = ctx.queryParam("password");
        String langsStr = ctx.queryParam("lang"); // should be "de,en"
        if (isNullOrEmpty(branch) || isNullOrEmpty(login) || isNullOrEmpty(password) || isNullOrEmpty(langsStr)) {
            throw new RuntimeException("Missing parameter");
        }

        List<String> langs = new ArrayList<>();
        for (String lang : langsStr.split(",")) {
            langs.add(lang);
        }
        if (langs.isEmpty()) {
            throw new RuntimeException("Parameter lang must not be empty!");
        }
        synchronized (handle) {
            UserSO userSO = new UserSO(new GitlabUser(login, password));
            WorkspaceSO workspace = userSO.getWorkspace(branch);
    
            File targetFolder = new PublishService(langs).publish(workspace);
            downloadFolderAsZip(targetFolder, ctx);
    
            Logger.info("PublishAction finished");
        }
    }
    
    public static void downloadFolderAsZip(File sourceFolder, Context ctx) {
        downloadFolderAsZip(sourceFolder, sourceFolder.getName(), ctx);
    }
    
    public static void downloadFolderAsZip(File sourceFolder, String name, Context ctx) {
        File zipFile = new File(sourceFolder.getParentFile(), name + ".zip");
        FileService.zip(sourceFolder, zipFile);
        
        ctx.res.type("application/zip");
        ctx.res.header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
        try {
            ctx.res.raw().getOutputStream().write(Files.readAllBytes(zipFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        zipFile.delete();
        FileService.deleteFolder(sourceFolder);
    }
}
