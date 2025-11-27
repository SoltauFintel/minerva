package minerva.base;

import minerva.model.StatesSO;
import minerva.user.UAction;

public class UpdatePagesMetricsAction extends UAction {

    @Override
    protected void execute() {
        StatesSO.updatePagesMetrics();
    }
    
    @Override
    protected String render() {
        return "ok";
    }
}
