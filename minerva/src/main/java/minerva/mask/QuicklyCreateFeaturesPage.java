package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import minerva.book.BookPage;
import minerva.seite.SPage;

public class QuicklyCreateFeaturesPage extends SPage {

	@Override
	protected void execute() {
		if (isPOST()) {
			String in = ctx.formParam("features");
			
			List<String> lines = new ArrayList<>();
			for (String line : in.split("\n")) {
				if (!line.isBlank()) {
					lines.add(line.trim());
				}
			}
			new QuicklyCreateFeaturesService().createFeatures(seite, lines);
			
			ctx.redirect(viewlink);
		} else {
			header(n("QuicklyCreateFeaturesTitle"));
            BookPage.oneLang(model, book);
		}
	}
}
