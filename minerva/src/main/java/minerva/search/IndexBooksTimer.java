package minerva.search;

import github.soltaufintel.amalia.timer.AbstractTimer;
import minerva.model.StatesSO;

public class IndexBooksTimer extends AbstractTimer {

    @Override
    protected void timerEvent() {
        StatesSO.login().masterWorkspace().getSearch().indexBooks();
    }
}
