package minerva.attachments;

import org.pmw.tinylog.Logger;

import minerva.user.UAction;

/** htmx POST endpoint */
public class SaveUserAttachmentCategoryAction extends UAction {

    @Override
    protected void execute() {
        String category = ctx.formParam("cat");
        Logger.debug(user.getLogin() + " | attachment category: " + category);
        if (category != null) {
            user.saveAttachmentCategory(category.trim().toLowerCase());
        }
    }
    
    @Override
    protected String render() {
        return "";
    }
}
