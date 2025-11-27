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
        branch = __branch();
        workspace = user.getWorkspace(branch);
    }
    
    protected String __branch() {
        return ctx.pathParam("branch");
    }
}
