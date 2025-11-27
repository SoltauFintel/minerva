package minerva.publish;

import static gitper.base.StringService.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.base.FileService;
import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.spark.Context.ContentDisposition;
import github.soltaufintel.amalia.web.action.Action;

public class PublishAction extends Action {
	private static final Object LOCK = new Object();

    @Override
    protected void execute() {
        String branch = ctx.queryParam("branch");
        String login = ctx.queryParam("login");
        String password = ctx.queryParam("password");
        String langs = ctx.queryParam("lang"); // should be "de,en"
        if (isNullOrEmpty(branch) || isNullOrEmpty(login) || isNullOrEmpty(password) || isNullOrEmpty(langs)) {
            throw new RuntimeException("Missing parameter");
        }
        
        synchronized (LOCK) {
            File targetFolder = new PublishService(langs).loginAndPublish(login, password, branch);
            downloadFolderAsZip(targetFolder, ctx);
        }
        Logger.info(login + " | " + branch + " | PublishAction finished");
    }
    
    public static void downloadFolderAsZip(File sourceFolder, Context ctx) {
        File pdfFile = new File(sourceFolder, sourceFolder.getName() + ".pdf");
        if (pdfFile.isFile()) {
            ctx.res.type("application/pdf");
            downloadFile(pdfFile, ctx);
        } else {
            File zipFile = new File(sourceFolder.getParentFile(), sourceFolder.getName() + ".zip");
            FileService.zip(sourceFolder, zipFile);
            ctx.res.type("application/zip");
            downloadFile(zipFile, ctx);
        }
        FileService.deleteFolder(sourceFolder);
    }
    
    private static void downloadFile(File file, Context ctx) {
        ctx.contentDisposition(ContentDisposition.attachment, file.getName());
        try {
            ctx.res.raw().getOutputStream().write(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        file.delete();
    }
}
