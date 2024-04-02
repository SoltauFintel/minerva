package minerva.book;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.web.action.IdAndLabel;
import minerva.base.NLS;
import minerva.base.NlsString;

public class EditBookPage extends BPage {

    @Override
    protected void execute() {
        user.onlyAdmin();
        
        if (isPOST()) {
            NlsString title = new NlsString();
            langs.forEach(lang -> title.setString(lang, ctx.queryParam("bookTitle" + lang)));
            
            book.getBook().setTitle(title);
            book.getBook().setType(BookType.valueOf(ctx.queryParam("type")));
            book.getBook().setPosition(Integer.parseInt(ctx.queryParam("position")));
            books.sort(); // Position könnte geändert sein
            books.save(book.cm("edit book"));
            user.log("Book saved. " + book.getBook().getFolder());

            ctx.redirect("/w/" + branch);
        } else {
            header(n("editBook"));
            put("bookTitlede", book.getBook().getFolder()); // for the case that language "de" isn't configured
            put("bookTitleen", book.getBook().getFolder());
            langs.forEach(lang -> put("bookTitle" + lang, book.getBook().getTitle().getString(lang)));
            putInt("position", book.getBook().getPosition());

            List<IdAndLabel> bookTypes = new ArrayList<>();
            for (BookType bookType : BookType.values()) {
                bookTypes.add(new BookTypeIAL(bookType, user.getGuiLanguage()));
            }
            combobox_idAndLabel("bookTypes", bookTypes, book.getBook().getType().name(), false);
        }
    }
    
    public static class BookTypeIAL implements IdAndLabel {
        private final BookType type;
        private final String guiLanguage;
        
        public BookTypeIAL(BookType type, String guiLanguage) {
            this.type = type;
            this.guiLanguage = guiLanguage;
        }

        @Override
        public String getId() {
            return type.name();
        }

        @Override
        public String getLabel() {
            return NLS.get(guiLanguage, "bookType_" + type.name());
        }
    }
}
