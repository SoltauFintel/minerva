package minerva.publish;

import static minerva.base.StringService.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Action;
import minerva.base.FileService;

public class PublishAction extends Action {
    private static String handle = "handle";

    @Override
    protected void execute() {
        String branch = ctx.queryParam("branch");
        String login = ctx.queryParam("login");
        String password = ctx.queryParam("password");
        String langs = ctx.queryParam("lang"); // should be "de,en"
        if (isNullOrEmpty(branch) || isNullOrEmpty(login) || isNullOrEmpty(password) || isNullOrEmpty(langs)) {
            throw new RuntimeException("Missing parameter");
        }
        
        synchronized (handle) {
            File targetFolder = new PublishService(langs).loginAndPublish(login, password, branch);
            downloadFolderAsZip(targetFolder, ctx);
        }
        Logger.info(login + " | " + branch + " | PublishAction finished");
    }
    
    public static void downloadFolderAsZip(File sourceFolder, Context ctx) {
    	File pdfFile = new File(sourceFolder, sourceFolder.getName() + ".pdf");
    	if (pdfFile.isFile()) {
	        ctx.res.type("application/pdf");
	        // TODO duplicate code
	        ctx.res.header("Content-Disposition", "attachment; filename=\"" + pdfFile.getName() + "\"");
	        try {
	            ctx.res.raw().getOutputStream().write(Files.readAllBytes(pdfFile.toPath()));
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	
	        pdfFile.delete();
    	} else {
	        File zipFile = new File(sourceFolder.getParentFile(), sourceFolder.getName() + ".zip");
	        FileService.zip(sourceFolder, zipFile);
	        
	        ctx.res.type("application/zip");
	        ctx.res.header("Content-Disposition", "attachment; filename=\"" + zipFile.getName() + "\"");
	        try {
	            ctx.res.raw().getOutputStream().write(Files.readAllBytes(zipFile.toPath()));
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	
	        zipFile.delete();
    	}
        FileService.deleteFolder(sourceFolder);
    }
}
