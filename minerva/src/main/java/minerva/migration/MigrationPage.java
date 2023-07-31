package minerva.migration;

import java.io.File;

import org.pmw.tinylog.Logger;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

public class MigrationPage extends UPage {

    @Override
    protected void execute() {
        String branch = ctx.pathParam("branch");
        Logger.info("Migrations started by " + user.getLogin() + " | branch: " + branch);
        if ("master".equals(branch)) {
            throw new RuntimeException("Migration nicht für master erlaubt! (Schutz)");
        }

        String sourceFolder = MinervaWebapp.factory().getConfig().getMigrationSourceFolder();
        if (StringService.isNullOrEmpty(sourceFolder)) {
            sourceFolder = "/";
        }
        Logger.info("source folder   : " + sourceFolder);
        
        String helpKeysFolder = MinervaWebapp.factory().getConfig().getMigrationHelpKeysFolder();
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
            ctx.redirect("/w/" + branch);
        }
        
        put("branch", esc(branch));
        put("sourceFolder", new File(sourceFolder).getAbsolutePath());
        put("helpKeysFolder", new File(helpKeysFolder).getAbsolutePath());
    }
}
