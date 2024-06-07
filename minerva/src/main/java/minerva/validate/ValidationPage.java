package minerva.validate;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;

public class ValidationPage extends BPage {

    @Override
    protected void execute() {
    	Logger.info("Validation of book " + book.getBook().getFolder());
    	
        ValidationResult result = new ValidatorService().start(book, langs, user.getGuiLanguage());

        header(n("validate"));
        DataList hauptliste = list("hauptliste");
		for (String lang : langs) {
			DataMap langEintrag = hauptliste.add();
			langEintrag.put("lang", lang);
			fillPages(result, lang, langEintrag);
			fillLinks(result, lang, langEintrag);
		}
		putInt("nPages", result.getSeitenCount());
		putInt("nMessages", result.getMessagesCount());
        fillUnusedImages(result);
    }

	private void fillPages(ValidationResult result, String lang, DataMap langEintrag) {
		DataList seiten = langEintrag.list("seiten");
		result.getSeiten().stream()
			.filter(s -> s.getLang().equals(lang))
			.sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
			.forEach(seite -> {
				DataMap map = seiten.add();
				map.put("title", esc(seite.getTitle()));
				map.put("link", seite.getLink());
				DataList fehlerliste = map.list("fehlerliste");
				for (String msg : seite.getFehlerliste()) {
					fehlerliste.add().put("text", esc(msg));
				}
			});
		langEintrag.put("hasEntries", !result.getSeiten().isEmpty());
	}

	private void fillLinks(ValidationResult result, String lang, DataMap langEintrag) {
		DataList links = langEintrag.list("links");
		result.getLinks().stream()
			.filter(l -> l.getLang().equals(lang))
			.sorted((a, b) -> a.getPagetitle().compareToIgnoreCase(b.getPagetitle()))
			.forEach(link -> {
				DataMap map = links.add();
				map.put("pagelink", link.getPagelink());
				map.put("pagetitle", esc(link.getPagetitle()));
				map.put("href", link.getHref());
				map.put("title", esc(link.getTitle()));
			});
		langEintrag.put("hasLinks", !result.getLinks().isEmpty());
	}

	private void fillUnusedImages(ValidationResult result) {
		DataList unusedImages = list("pagesWithUnusedImages");
		result.getUnusedImages().stream()
			.sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
			.forEach(page -> {
	        	DataMap map = unusedImages.add();
	        	map.put("title", esc(page.getTitle()));
	        	map.put("link", page.getLink());
	        	DataList list2 = map.list("unusedImages");
				for (String dn : page.getUnusedImages()) {
					list2.add().put("dn", esc(dn));
				}
			});
        put("hasUnusedImages", !unusedImages.isEmpty());
	}
}
