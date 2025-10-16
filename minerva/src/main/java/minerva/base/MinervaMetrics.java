package minerva.base;

import java.util.Arrays;

import gitper.apm.Counter;
import gitper.apm.Gauge;
import gitper.apm.Histogram;

public interface MinervaMetrics {

	Counter LOGIN = new Counter("minerva_login");
	Counter LOGOUT = new Counter("minerva_logout");

	Counter MENU = new Counter("minerva_menu");

	Counter PAGE_VIEW = new Counter("minerva_page_view");
	
	Counter PAGE_LOADED = new Counter("minerva_page_loaded");
	
	Gauge PAGES_IN_MEMORY = new Gauge("minerva_inmemory_pages");
	Gauge BOOKS_IN_MEMORY = new Gauge("minerva_inmemory_books");
	Gauge WORKSPACES_IN_MEMORY = new Gauge("minerva_inmemory_workspaces");
	Gauge USERS_IN_MEMORY = new Gauge("minerva_inmemory_users");
	
	Counter ERRORPAGE = new Counter("minerva_errorpage");
	Counter ERRORPAGE404 = new Counter("minerva_errorpage404");

	Counter JOURNAL = new Counter("minerva_journal");

	Counter EXPORT = new Counter("minerva_export");

	Counter SEARCH = new Counter("minerva_search");
	Counter REINDEX = new Counter("minerva_search_reindex");

	Histogram PAGE_SAVETIME = new Histogram("minerva_page_savetime4", "ms", Arrays.asList(500l, 1000l, 2000l, 3000l, 4000l, 5000l));

	Gauge TOSMAP_SIZE = new Gauge("minerva_tosmap_size");
}
