package minerva.validate;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.validate.ValidationResult.VRLink;
import minerva.validate.ValidationResult.VRSeite;
import minerva.validate.ValidationResult.VRUnusedImageSeite;

public class ValidationPage extends BPage {

    @Override
    protected void execute() {
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
		for (VRSeite seite : result.getSeiten()) {
			if (seite.getLang().equals(lang)) {
				DataMap map = seiten.add();
				map.put("title", esc(seite.getTitle()));
				map.put("link", seite.getLink());
				DataList list2 = map.list("fehlerliste");
				for (String msg : seite.getFehlerliste()) {
					list2.add().put("text", esc(msg));
				}
			}
		}
		langEintrag.put("hasEntries", !result.getSeiten().isEmpty());
	}

	private void fillLinks(ValidationResult result, String lang, DataMap langEintrag) {
		DataList links = langEintrag.list("links");
		for (VRLink link : result.getLinks()) {
			if (link.getLang().equals(lang)) {
				DataMap list2 = links.add();
				list2.put("pagelink", link.getPagelink());
				list2.put("pagetitle", esc(link.getPagetitle()));
				list2.put("href", link.getHref());
				list2.put("title", esc(link.getTitle()));
			}
		}
		langEintrag.put("hasLinks", !result.getLinks().isEmpty());
	}

	private void fillUnusedImages(ValidationResult result) {
		DataList unusedImages = list("pagesWithUnusedImages");
        for (VRUnusedImageSeite page : result.getUnusedImages()) {
        	DataMap map = unusedImages.add();
        	map.put("title", esc(page.getTitle()));
        	map.put("link", page.getLink());
        	DataList list2 = map.list("unusedImages");
			for (String dn : page.getUnusedImages()) {
				list2.add().put("dn", esc(dn));
			}
		}
        put("hasUnusedImages", !unusedImages.isEmpty());
	}

}
