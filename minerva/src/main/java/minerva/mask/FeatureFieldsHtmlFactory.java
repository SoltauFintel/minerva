package minerva.mask;

import minerva.model.SeiteSO;

public class FeatureFieldsHtmlFactory {
	public static FeatureFieldsHtmlFactory FACTORY = new FeatureFieldsHtmlFactory();
	
	public FeatureFieldsHtml build(SeiteSO seite, boolean editMode) {
		return new FeatureFieldsHtml(seite, editMode);
	}
}
