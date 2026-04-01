package minerva.book.multiselect;

import java.util.Set;

import minerva.model.BookSO;

/**
 * MultiSelectPage action
 */
public interface MultiSelectChange { // Ich habe es Change genannt, um es nicht Action nennen zu müssen, da es bereits Amalia Actions gibt.

    /**
     * @return RB key für Action Combobox, gleichzeitig auch Identifizierung
     */
    String getLabelKey();
    
    void execute(BookSO book, Set<String> selectedPages, String tag);
}
