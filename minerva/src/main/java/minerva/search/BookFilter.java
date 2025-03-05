package minerva.search;

public interface BookFilter extends Comparable<BookFilter> {

	String getTitle();
	
	String getBookFilterId();
	
	@Override
	default int compareTo(BookFilter o) {
		return getBookFilterId().compareToIgnoreCase(o.getBookFilterId());
	}
}
