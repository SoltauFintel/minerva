package minerva;

import org.pmw.tinylog.Logger;

import github.soltaufintel.amalia.web.action.Page;

public class Eddi extends Page {

    @Override
    protected void execute() {
        Logger.info("Eddi");
        
    }
    
    @Override
    protected String render() {
        String h = super.render();
        Logger.info(h);
        return h;
    }
}
