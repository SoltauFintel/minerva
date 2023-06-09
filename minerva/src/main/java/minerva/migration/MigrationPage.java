package minerva.migration;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.base.StringService;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class MigrationPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        Logger.info("Migrations started by " + user.getUser().getLogin() + " | branch: " + branch);
        if ("master".equals(branch)) {
            throw new RuntimeException("Migration nicht für master erlaubt! (Schutz)");
        }

        String sourceFolder = System.getenv("MINERVA_MIGRATIONSOURCEFOLDER");
        if (StringService.isNullOrEmpty(sourceFolder)) {
            sourceFolder = "/";
        }
        Logger.info("source folder   : " + sourceFolder);
        
        String helpKeysFolder = System.getenv("MINERVA_MIGRATIONHELPKEYSFOLDER");
        if (StringService.isNullOrEmpty(helpKeysFolder)) {
            helpKeysFolder = "/html/mappings2";
        }
        Logger.info("help keys folder: " + helpKeysFolder);

        if ("1".equals(ctx.queryParam("m"))) {
            WorkspaceSO workspace = user.getWorkspace(branch);
            try {
                new ConfluenceToMinervaMigrationService(new File(sourceFolder), new File(helpKeysFolder),
                        workspace, langs).migrate();
            } catch (Exception e) {
                Logger.error(e);
                throw new RuntimeException("Migration error. See log.");
            }
            ctx.redirect("/b/"+ branch);
        }
        
        put("branch", esc(branch));
        put("sourceFolder", new File(sourceFolder).getAbsolutePath());
        put("helpKeysFolder", new File(helpKeysFolder).getAbsolutePath());
    }
}
