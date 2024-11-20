package minerva.seite;

import com.github.template72.data.IDataMap;

import minerva.MinervaWebapp;
import minerva.base.NLS;
import minerva.model.SeiteSO;
import minerva.model.UserSO;

public class PageMenuContext {
	private final String guiLanguage;
	private final SeiteSO seite;
	private final boolean isAdmin;
	private final boolean isFavorite;
	private final boolean pageWatched;
	private final boolean subpagesWatched;
	private final boolean gitlab = MinervaWebapp.factory().getConfig().isGitlab();
	private final boolean isCustomerVersion = MinervaWebapp.factory().isCustomerVersion();
	private final boolean customerMode;
	private final IDataMap model;
	
	public PageMenuContext(SeiteSO seite, boolean isAdmin, boolean isFavorite, boolean pageWatched, boolean subpagesWatched, IDataMap model) {
		UserSO user = seite.getBook().getWorkspace().getUser();
		this.guiLanguage = user.getGuiLanguage();
		this.seite = seite;
		this.isAdmin = isAdmin;
		this.isFavorite = isFavorite;
		this.pageWatched = pageWatched;
		this.subpagesWatched = subpagesWatched;
		this.customerMode = user.getCustomerMode().isActive();
		this.model = model;
	}

	public String getGuiLanguage() {
		return guiLanguage;
	}

	public SeiteSO getSeite() {
		return seite;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	public boolean isPageWatched() {
		return pageWatched;
	}

	public boolean isSubpagesWatched() {
		return subpagesWatched;
	}

	public boolean isGitlab() {
		return gitlab;
	}

	public boolean isCustomerVersion() {
		return isCustomerVersion;
	}

	public boolean isCustomerMode() {
		return customerMode;
	}

	public String get(String var) {
		return model.get(var).toString();
	}

	public String n(String key) {
		return NLS.get(guiLanguage, key);
	}
}
