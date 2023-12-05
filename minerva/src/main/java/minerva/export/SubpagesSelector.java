package minerva.export;

import minerva.model.SeiteSO;

public interface SubpagesSelector {

	Iterable<SeiteSO> getSubpages(SeiteSO seite);
}