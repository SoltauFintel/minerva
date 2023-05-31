package minerva.seite.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TagNList {
	private final List<TagN> list = new ArrayList<>();

	public List<TagN> sortedByTag() {
		return list.stream()
				.sorted((a, b) -> a.getTag().compareTo(b.getTag()))
				.collect(Collectors.toList());
	}

	public List<TagN> sortedByN() {
		return list.stream()
				.sorted((a, b) -> {
					int r = Integer.valueOf(b.getAnzahl()).compareTo(Integer.valueOf(a.getAnzahl()));
					if (r == 0) {
						r = a.getTag().compareTo(b.getTag());
					}
					return r;
				}).collect(Collectors.toList());
	}

	public void add(String tag) {
		for (TagN i : list) {
			if (i.getTag().equals(tag)) {
				i.setAnzahl(i.getAnzahl() + 1);
				return;
			}
		}
		TagN neu = new TagN();
		neu.setAnzahl(1);
		neu.setTag(tag);
		list.add(neu);
	}
}
