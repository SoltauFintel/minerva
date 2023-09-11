package minerva.releasenotes;

import github.soltaufintel.amalia.web.templating.ColumnFormularGenerator;
import github.soltaufintel.amalia.web.templating.TemplatesInitializer;
import minerva.book.BPage;

/**
 * Enter Confluence space key (=customer) and title of "Release notes" page for importing release notes
 */
public class SelectRNCustomerPage extends BPage {

	@Override
	protected void execute() {
		// TODO MinervaWebapp.factory().gitlabOnlyPage();
        if (isPOST()) {
        	String spaceKey = ctx.formParam("spaceKey");
        	String rootTitle = ctx.formParam("rootTitle");
        	ctx.redirect(booklink + "/rn-select-release?s=" + u(spaceKey) + "&t=" + u(rootTitle));
        } else {
        	header(n("loadReleaseNotes"));
        	put("spaceKey", "BASF");
        	put("rootTitle", "Release notes");
            ColumnFormularGenerator gen = new ColumnFormularGenerator(2, 1);
            initColumnFormularGenerator(gen);
            TemplatesInitializer.fp.setContent(gen
            		.textfield("spaceKey", n("spacekey"), 1)
            		.textfield("rootTitle", n("title"), 2)
                    .save(n("Forward"))
                    .getHTML(booklink + "/rn-select-customer", booklink));
        }
	}
	
	@Override
	protected String getPage() {
		return "formular/" + super.getPage();
		// ReleaseNotes2Page uses same template
	}
}
