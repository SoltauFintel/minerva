package minerva.config;

import github.soltaufintel.amalia.web.action.Action;
import minerva.MinervaWebapp;

public class InfoAction extends Action {
    
    @Override
    protected void execute() {
    }
    
    @Override
    protected String render() {
        return "Minerva " + MinervaWebapp.VERSION;
    }
}
