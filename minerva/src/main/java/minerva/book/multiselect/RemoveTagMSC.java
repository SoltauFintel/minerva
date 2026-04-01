package minerva.book.multiselect;

import java.util.Set;

import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.model.BookSO;

public class RemoveTagMSC implements MultiSelectChange {

    @Override
    public String getLabelKey() {
        return "removetag";
    }

    @Override
    public void execute(BookSO book, Set<String> selectedPages, String tag) {
        if (StringService.isNullOrEmpty(tag)) {
            throw new UserMessage("multiSelectError2", book.getUser());
        }
        book.multiSelectAction(selectedPages, tag, false, false);
    }
}
