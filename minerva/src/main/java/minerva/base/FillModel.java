package minerva.base;

public interface FillModel<S, T> {

	/**
	 * @param source e.g. SeiteSO
	 * @param target e.g. DataList
	 */
	void fill(S source, T target);
}
