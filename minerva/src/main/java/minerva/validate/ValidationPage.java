package minerva.validate;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;

import minerva.book.BPage;
import minerva.model.SeiteSO;
import minerva.seite.Breadcrumb;
import minerva.seite.ViewAreaBreadcrumbLinkBuilder;

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
			fillSameTitles(result, lang, langEintrag);
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

	private void fillSameTitles(ValidationResult result, String lang, DataMap langEintrag) {
		DataList list = langEintrag.list("sameTitles");
		Set<Entry<String, List<SeiteSO>>> entrySet = result.getSameTitles().entrySet();
		entrySet.stream()
			.filter(e -> e.getKey().startsWith(lang + ":"))
			.sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
			.forEach(e -> {
				DataMap map = list.add();
				int o = e.getKey().indexOf(":");
				map.put("title", e.getKey().substring(o + 1));
				DataList seiten = map.list("seiten");
				for (SeiteSO seite : e.getValue()) {
					DataMap map2 = seiten.add();
					map2.put("title", esc(seite.getSeite().getTitle().getString(lang)));
					map2.put("breadcrumbs", esc(breadcrumbs(seite, lang)));
					int hc = seite.hasContent(lang);
					map2.put("empty", hc == 0 || hc == 2);
					map2.put("link", seite.viewlink());
					DataList list3 = map2.list("tags");
					for (String tag : seite.getSeite().getTags()) {
						list3.add().put("tag", esc(tag));
					}
				}
			});
		langEintrag.put("hasSameTitles", entrySet.stream().anyMatch(e -> e.getKey().startsWith(lang + ":")));
	}
	
	private String breadcrumbs(SeiteSO seite, String lang) {
		String ret = "";
        List<Breadcrumb> breadcrumbs = seite.getBook().getBreadcrumbs(seite.getId(), new ViewAreaBreadcrumbLinkBuilder());
        for (int i = breadcrumbs.size() - 1; i >= 0; i--) {
            Breadcrumb b = breadcrumbs.get(i);
            if (!ret.isEmpty()) {
            	ret += " > ";
            }
            ret += b.getTitle().getString(lang);
        }
        return ret;
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
	        	page.getUnusedImages().stream().sorted().forEach(dn -> list2.add().put("dn", esc(dn)));
			});
        put("hasUnusedImages", !unusedImages.isEmpty());
	}
}
