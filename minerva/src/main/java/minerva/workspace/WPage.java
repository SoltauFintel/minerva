package minerva.workspace;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.WorkspaceSO;
import minerva.user.UPage;

/**
 * Base class for workspace pages
 */
public abstract class WPage extends UPage {
    protected String branch;
    protected WorkspaceSO workspace;
    
    @Override
    public void init(Context ctx) {
        super.init(ctx);

        branch = __branch();
        user.setCurrentWorkspace(workspace = user.getWorkspace(branch));

        put("branch", branch);
        put("isMasterBranch", "master".equals(branch));
    }
    
    protected String __branch() {
        return ctx.pathParam("branch");
    }
}
