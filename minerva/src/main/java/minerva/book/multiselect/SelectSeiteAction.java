package minerva.book.multiselect;

import org.pmw.tinylog.Logger;

import minerva.book.BAction;

// HTMX
public class SelectSeiteAction extends BAction {

    @Override
    protected void execute() {
        String id = ctx.queryParam("id");
        boolean checked = ctx.formParam("v") != null;
        
        book.saveMultiSelect(id, checked);
        Logger.debug(book.getUser().getLogin() + " | SelectSeite(" + checked + ") " + id);
    }
}
