package minerva.workspace;

import github.soltaufintel.amalia.spark.Context;
import minerva.model.WorkspaceSO;
import minerva.user.UAction;

public abstract class WAction extends UAction {
    protected String branch;
    protected WorkspaceSO workspace;
    
    @Override
    public void init(Context ctx) {
        super.init(ctx);
        branch = ctx.pathParam("branch");
        workspace = user.getWorkspace(branch);
    }
}
