package minerva.book.multiselect;

import java.util.Set;

import gitper.base.StringService;
import minerva.base.UserMessage;
import minerva.model.BookSO;

public class ClearTagsMSC implements MultiSelectChange {

    @Override
    public String getLabelKey() {
        return "cleartags";
    }

    @Override
    public void execute(BookSO book, Set<String> selectedPages, String tag) {
        if (!StringService.isNullOrEmpty(tag)) { // Bedienungsschutz
            throw new UserMessage("multiSelectError3", book.getUser());
        }
        book.multiSelectAction(selectedPages, tag, true, false);
    }
}
