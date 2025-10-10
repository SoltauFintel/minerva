package minerva.base;

import java.util.Arrays;

import gitper.apm.Histogram;

public interface MinervaMetrics {

	Histogram PAGE_SAVETIME = new Histogram("minerva_page_savetime4", "ms", Arrays.asList(500l, 1000l, 2000l, 3000l, 4000l, 5000l));
}
