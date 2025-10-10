package minerva.base;

import java.util.Arrays;

import gitper.apm.Counter;
import gitper.apm.Histogram;

public interface MinervaMetrics {

	Counter MENU = new Counter("minerva_menu");

	Counter PAGE_VIEW = new Counter("minerva_page_view");
	
	Counter PAGE_LOADED = new Counter("minerva_page_loaded");
	
	Counter ERRORPAGE = new Counter("minerva_errorpage");
	Counter ERRORPAGE404 = new Counter("minerva_errorpage404");

	Counter JOURNAL = new Counter("minerva_journal");

	Counter EXPORT = new Counter("minerva_export");
	
	Histogram PAGE_SAVETIME = new Histogram("minerva_page_savetime4", "ms", Arrays.asList(500l, 1000l, 2000l, 3000l, 4000l, 5000l));
}
