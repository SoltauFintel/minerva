package minerva.keyvalue;

import org.pmw.tinylog.Logger;

import minerva.workspace.WAction;

public class DeleteValuesAction extends WAction {
    
    @Override
    protected void execute() {
        String key = ctx.pathParam("key");
        
        Logger.info("DeleteValues key=" + key + ", branch=" + branch);
        new ValuesSO(workspace).delete(key);
        
        ctx.redirect("/values/" + branch);
    }
}
