package minerva.base;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * ArrayList like class with reduced methods
 */
public class MList<T> implements Iterable<T> {
	private final List<T> list = new ArrayList<>();
	private final Comparator<T> comparator;
	
	public MList() {
		this(null);
	}

	public MList(Comparator<T> comparator) {
		this.comparator = comparator;
	}

	public void add(T t) {
		list.add(t);
		sort();
	}
	
	public void sort() {
		if (comparator != null) {
			list.sort(comparator);
		}
	}
	
	public T get(int index) {
		return list.get(index);
	}
	
	public int size() {
		return list.size();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
	
	protected boolean remove(T t) {
		return list.remove(t);
	}

	public void order(List<String> idList, MListOrder<T> eq) {
		List<T> newList = new ArrayList<>();
		for (String id : idList) {
			for (T t : list) {
				if (eq.isEqual(t, id)) {
					newList.add(t);
					break;
				}
			}
		}
		list.clear();
		list.addAll(newList);
	}
	
	public interface MListOrder<T> {
		
		boolean isEqual(T t, String id);
	}
}
