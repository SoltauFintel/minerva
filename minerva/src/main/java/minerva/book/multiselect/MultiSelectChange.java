package minerva.book.multiselect;

import java.util.Set;

import minerva.model.BookSO;

public interface MultiSelectChange { // Ich habe es Change genannt, um es nicht Action nennen zu müssen.
    
    // TODO Mehrsprachigkeit ist ein Problem. Ich muss dann mit ID+Label arbeiten.
    String getLabelKey();
    
    void execute(BookSO book, Set<String> selectedPages, String tag);
}
