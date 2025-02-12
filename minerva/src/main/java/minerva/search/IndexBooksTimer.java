package minerva.search;

import minerva.base.AbstractTimer;
import minerva.model.StatesSO;

public class IndexBooksTimer extends AbstractTimer {

	@Override
	protected void timerEvent() {
		StatesSO.login().masterWorkspace().getSearch().indexBooks();
	}
}
