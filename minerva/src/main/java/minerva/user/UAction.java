package minerva.user;

import java.util.List;

import github.soltaufintel.amalia.spark.Context;
import github.soltaufintel.amalia.web.action.Action;
import minerva.MinervaWebapp;
import minerva.model.StatesSO;
import minerva.model.UserSO;

/**
 * Base class for user actions
 */
public abstract class UAction extends Action {
	protected UserSO user;
	protected List<String> langs;
	
	@Override
	public void init(Context ctx) {
		super.init(ctx);
		user = StatesSO.get(ctx).getUser();
		langs = MinervaWebapp.factory().getLanguages();
	}
}
