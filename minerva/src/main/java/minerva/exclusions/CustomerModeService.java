package minerva.exclusions;

import minerva.MinervaWebapp;
import minerva.base.StringService;
import minerva.model.SeiteSO;
import minerva.model.WorkspaceSO;

public class CustomerModeService {
    private final ExclusionsService sv;

    public CustomerModeService(WorkspaceSO workspace) {
        String cm = workspace.getUser().getUser().getCustomerMode();
        if (MinervaWebapp.factory().isCustomerVersion() || StringService.isNullOrEmpty(cm)) {
            sv = null;
        } else {
            sv = new ExclusionsService();
            sv.setExclusions(new Exclusions(workspace.getExclusions().get()));
            sv.setCustomer(cm);
        }
    }
    
    public boolean isActive() {
        return sv != null;
    }

    public boolean isAccessible(SeiteSO seite) {
        return sv == null || sv.isAccessible(seite.getSeite().getTags());
    }

    public ExclusionsService getExclusionsService() {
        return sv;
    }
}
