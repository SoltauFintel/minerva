package minerva.book;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pmw.tinylog.Logger;

import com.google.gson.Gson;

import minerva.access.CommitMessage;
import minerva.base.FileService;
import minerva.base.StringService;
import minerva.mask.FeatureFields;
import minerva.mask.FeatureFieldsService;
import minerva.model.BookSO;
import minerva.model.SeiteSO;
import minerva.model.SeitenSO;

// XXX temp.
public class MenuStructureAction extends BAction {
    private final Gson gson = new Gson();
    private int nextFeatureNumber;
    
    @Override
    protected void execute() {
//        String mode = ctx.queryParam("m");
        
//        if ("input".equals(mode)) {
            Logger.info("Menu... input");
            doImport();
//        } else { // output
//            Logger.info("Menu... output");
//            
//            StringBuilder sb = new StringBuilder();
//            add(book.getSeiten(), sb, "");
//            
//            File out = new File("export/menu.txt");
//            FileService.savePlainTextFile(out, sb.toString());
//            Logger.info(out.getAbsolutePath());
//        }
    }
    
    private void doImport() {
        File in = new File("export/menu.txt");
        String text = FileService.loadPlainTextFile(in);
		List<Item> items = getItems(text);
		
		findPages(items, book.getSeiten());

		BookSO target = book.getWorkspace().getBooks().byFolder("featuretree");
		SeiteSO targetPage = target.seiteById("w3i9ca"); // "Menü"
		Map<String, String> files = new HashMap<>();
		targetPage.getSeite().setSorted(false);
		targetPage.saveMetaTo(files);
		nextFeatureNumber = 1;
		createFeatures(items, targetPage, true, files);
		Logger.info("saving " + files.size() + " files...");
		target.dao().saveFiles(files, new CommitMessage("Saved menu structure as features"), book.getWorkspace());
		Logger.info("saved");
	}

    private void createFeatures(List<Item> items, SeiteSO parentPage, boolean topLevel, Map<String,String> files) {
    	int c = 0;
		for (Item item : items) {
			if (topLevel) {
				c += 100;
				nextFeatureNumber = c;
				Logger.info(item.getTitle() + " = FMP" + new DecimalFormat("0000").format(nextFeatureNumber));
			}
			if (item.getFeatures() != null && item.getItems().isEmpty()) {
				// many features for one page
				for (String feature : item.getFeatures().split(",")) {
					if (!feature.isBlank()) {
						createFeature(item, feature, parentPage, files);
					}
				}
			} else {
				if (item.getFeatures() != null) {
					Logger.warn("don't split features: " + item.getFeatures());
				}
				SeiteSO seite = createFeature(item, item.getTitle(), parentPage, files);
				createFeatures(item.getItems(), seite, false, files); // recursive
			}
		}
    }
    
	private SeiteSO createFeature(Item item, String title, SeiteSO parentPage, Map<String, String> files) {
		String seiteId = parentPage.getSeiten().createSeite(parentPage, parentPage.getBook(), parentPage.getBook().dao());
		SeiteSO seite = parentPage.getSeiten().byId(seiteId);

		seite.getSeite().setSorted(false);
		seite.getSeite().getTitle().setString("de", title);
		seite.getSeite().getTitle().setString("en", title);

		seite.saveMetaTo(files);

		FeatureFields ff = FeatureFields.create(seite);
		if (item.getItems().isEmpty()) {
			ff.set("featurenumber", "FMP" + new DecimalFormat("0000").format(nextFeatureNumber++));
		}
		if (!StringService.isNullOrEmpty(item.getSeiteId())) {
			ff.getPages().add(item.getSeiteId()); // link to handbuch page
		}
		files.put(FeatureFieldsService.dn(seite), gson.toJson(ff));

		return seite;
	}

	private void findPages(List<Item> items, SeitenSO seiten) {
		for (Item item : items) {
			SeiteSO seite = null;
			for (SeiteSO i : seiten) {
				if (i.getTitle().equals(item.getTitle())) {
					seite = i;
					break;
				}
			}
			if (seite == null) {
				Logger.error("Can't find page: \"" + item.getTitle() + "\"!");
			} else {
				item.setSeiteId(seite.getId());
				findPages(item.getItems(), seite.getSeiten()); // recursive
			}
		}
    }
    
	private List<Item> getItems(String text) {
		List<Item> items = new ArrayList<>();
        int lineNo = 0;
		for (String line : text.replace("\r\n", "\n").split("\n")) {
			lineNo++;
			try {
				int ebene = line.indexOf("- ") / 2; // ab 0
				String title = line.substring(line.indexOf("- ") + 2);
				int o = title.indexOf("|");
				String features = null;
				if (o >= 0) {
					features = title.substring(o + 1);
					title = title.substring(0, o);
				}
				Item item = new Item();
				item.setTitle(title);
				item.setFeatures(features);
				if (ebene == 0) {
					items.add(item);
				} else if (ebene == 1) {
					Item parent = items.get(items.size() - 1);
					parent.getItems().add(item);
				} else if (ebene >= 2) {
					Item parent = items.get(items.size() - 1);
					for (int i = 1; i < ebene; i++) {
						parent = parent.getItems().get(parent.getItems().size() - 1);
					}
					parent.getItems().add(item);
				}
			} catch (Exception e) {
				throw new RuntimeException("Error in line #" + lineNo + ": " + line //
						+ "\n" + e.getClass().getName() + ": " + e.getMessage(), e);
			}
        }
		return items;
	}

	private void add(SeitenSO seiten, StringBuilder sb, String indent) {
        for (SeiteSO seite : seiten) {
            sb.append(indent + "- " + seite.getTitle() + "\n");
            add(seite.getSeiten(), sb, indent + "  "); // recursive
        }
    }

	
	public static class Item {
		private String title;
		private final List<Item> items = new ArrayList<>();
		private String features;
		private String seiteId;

		public String getFeatures() {
			return features;
		}

		public void setFeatures(String features) {
			this.features = features;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public List<Item> getItems() {
			return items;
		}

		public String getSeiteId() {
			return seiteId;
		}

		public void setSeiteId(String seiteId) {
			this.seiteId = seiteId;
		}
	}
}
