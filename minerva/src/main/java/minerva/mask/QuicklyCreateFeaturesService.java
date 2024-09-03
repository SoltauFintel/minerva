package minerva.mask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class QuicklyCreateFeaturesService {
	private static final Gson gson = new Gson();
	
	public void createFeatures(SeiteSO parent0, List<String> features) {
		Map<String, String> files = new HashMap<>();
		Map<String, SeiteSO> pm = new HashMap<>();
		pm.put("0", parent0);
		BookSO book = parent0.getBook();
		for (String line : features) {
			int indent = 0;
			while (line.startsWith("-")) {
				indent++;
				line = line.substring(1);
			}
			line = line.trim();
			int o = line.lastIndexOf("#");
			String featureNumber = "";
			if (o > 0) {
				featureNumber = line.substring(o + 1).trim().toUpperCase();
				line = line.substring(0, o).trim();
			}

			SeiteSO parent = pm.get("" + indent);
			if (parent == null) {
				book.getWorkspace().pull();
				throw new RuntimeException("No parent page for feature \"" + line + "\".");
			}
			String id = parent.getSeiten().createSeite(parent, book, book.dao());
			SeiteSO seite = parent.getSeiten()._byId(id);
			seite.getSeite().getTitle().setString("de", line);
			seite.getSeite().getTitle().setString("en", line);
			seite.getSeite().setSorted(false);
			seite.saveMetaTo(files);
			FeatureFields ff = FeatureFields.create(seite);
			if (!featureNumber.isBlank()) {
				ff.set(FeatureFields.FEATURENUMBER, featureNumber);
			}
			files.put(FeatureFieldsService.dn(seite), gson.toJson(ff));
			
			pm.put("" + (indent + 1), seite);
		}
		book.dao().saveFiles(files, new CommitMessage(parent0, "Quickly Create Features"), book.getWorkspace());
	}
}
