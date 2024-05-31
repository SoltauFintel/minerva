package minerva.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Escaper;
import minerva.base.FileService;
import minerva.base.UserMessage;
import minerva.workspace.WPage;

public class DownloadExportPage extends WPage {

    @Override
    protected void execute() {
        String id = ctx.pathParam("id");
        String mode = ctx.queryParam("mode");
        
        if ("dl".equals(mode)) {
            render = false;
            File file = GenericExportService.pop(id);
            if (file != null && file.isFile()) {
                if (file.getName().endsWith(".pdf")) {
                    ctx.res.type("application/pdf");
                    download(file);
                } else if (file.getName().endsWith(".zip")) {
                    ctx.res.type("application/zip");
                    download(file);
                }
            } else {
                throw new UserMessage("export-already-downloaded", user);
            }
        } else {
            String dn = GenericExportService.getFilename(id);
            if (dn == null) {
                throw new UserMessage("export-already-downloaded", user);
            }
            Logger.info("DownloadExportPage: " + dn);
            put("id", esc(id));
            put("dn", esc(dn));
            put("dnu", u(dn));
            put("downloadIcon", dn.endsWith(".pdf") ? "file-pdf-o" : "download");
        }
    }
    
    private void download(File file) {
        ctx.res.header("Content-Disposition", "inline; filename=\"" + file.getName() + "\""); // inline -> opens PDF in new tab instead of showing it in the browser download list
        try {
            ctx.res.raw().getOutputStream().write(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Logger.info("delete folder: " + file.getParentFile().getAbsolutePath());
        if (file.getParentFile().getParentFile().getName().startsWith("export_")) {
            FileService.deleteFolder(file.getParentFile().getParentFile());
        } else {
            FileService.deleteFolder(file.getParentFile());
        }
    }
    
    public static void redirectToThisPage(Context ctx, String branch, String id) {
        ctx.redirect("/w/" + Escaper.esc(branch) + "/download-export/" + Escaper.esc(id) + "/" + Escaper.urlEncode(GenericExportService.getFilename(id), ""));
    }
}
