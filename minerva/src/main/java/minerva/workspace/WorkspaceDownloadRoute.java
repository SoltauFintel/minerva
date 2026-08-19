package minerva.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.base.StringService;
import github.soltaufintel.amalia.web.route.Route;
import minerva.MinervaWebapp;
import minerva.model.StateSO;
import minerva.user.User;

// Admin Aktion: Workspace herunterladen zwecks Backup
public class WorkspaceDownloadRoute extends Route<Object> {
    private Object response;

    @Override
    protected void execute() {
        String token = MinervaWebapp.factory().getConfig().getAdminToken();
        if (StringService.isNullOrEmpty(token)) {
            ctx.status(401);
            Logger.error("WorkspaceDownloadRoute not accessible because ADMIN_TOKEN is not defined.");
            return;
        }
        String authHeader = ctx.req.headers("Authorization");
        if (authHeader == null || !authHeader.equals("Bearer " + token)) {
            ctx.status(401); // Unauthorized
            Logger.error("WorkspaceDownloadRoute not accessible because authorization failed.");
            return;
        }
        if (MinervaWebapp.factory().isGitlab()) {
            ctx.status(500);
            Logger.error("WorkspaceDownloadRoute is not for Gitlab mode");
            return;
        }
        User user = new User();
        user.setLogin("WorkspaceDownloadRoute");
        var workspace = new StateSO(user).getUser().masterWorkspace();
        if (workspace.getBooks().isEmpty()) {
            ctx.status(500);
            Logger.warn("WorkspaceDownloadRoute: no books -> can't download workspace");
            return;
        }
        try {
            File zipFile = Files.createTempFile("minerva-workspace-download-", ".zip").toFile();
            FileService.zip(new File(workspace.getFolder()), zipFile);
            response = FileService.loadBinaryFile(zipFile);
            zipFile.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ctx.res.header("Content-Type", "application/zip");
        ctx.res.header("Cache-Control", "max-age=" + (2 * 60)); // 2 minutes
    }

    @Override
    protected Object render() {
        return response;
    }
}
