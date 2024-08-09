package minerva.validate;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.pmw.tinylog.Logger;

import com.github.template72.data.DataList;
import com.github.template72.data.DataMap;
import com.github.template72.data.DataValue;
import com.github.template72.data.IDataCondition;
import com.github.template72.data.IDataList;
import com.github.template72.data.IDataMap;

import minerva.book.BPage;
import minerva.exclusions.SeiteSichtbar;
import minerva.exclusions.SeiteSichtbarContext;
import minerva.exclusions.Visible;
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
					fehlerliste.add().put("text", msg); // no esc()!
				}
			});
		langEintrag.put("hasEntries", result.getSeiten().stream().anyMatch(s -> s.getLang().equals(lang)));
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
		langEintrag.put("hasLinks", result.getLinks().stream().anyMatch(l -> l.getLang().equals(lang)));
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
					SeiteSichtbarContext ssc = new SeiteSichtbarContext(seite.getBook().getWorkspace(), List.of(lang));
					ssc.setShowAllPages(true);
					Visible visible = new SeiteSichtbar(seite, ssc).getVisibleResult();
					map2.put("empty", visible.hasSubpages() || visible.isShowAllPages()); 
					map2.put("link", seite.viewlink());
					DataList list3 = map2.list("tags");
					for (String tag : seite.getSeite().getTags()) {
						list3.add().put("tag", esc(tag));
					}
				}
				seiten.sort((a, b) ->        ((DataValue) a.get("breadcrumbs")).toString()
						.compareToIgnoreCase(((DataValue) b.get("breadcrumbs")).toString() ));
			});
		removeEmptyPairs(list);
		langEintrag.put("hasSameTitles", entrySet.stream().anyMatch(e -> e.getKey().startsWith(lang + ":")));
	}

	private void removeEmptyPairs(DataList list) {
		Iterator<IDataMap> iter = list.iterator();
		while (iter.hasNext()) {
			IDataMap i = iter.next();
			boolean remove = true;
			IDataList list2 = (IDataList) i.get("seiten");
			for (IDataMap j : list2) {
				boolean empty = ((IDataCondition) j.get("empty")).isTrue();
				if (!empty) {
					remove = false;
				}
			}
			if (remove) {
				iter.remove();
			}
		}
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
