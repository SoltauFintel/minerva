package minerva.mask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import minerva.mask.FeatureRelationsService.Relation;

/**
 * FeatureFieldsHtml relations column (eine Spalte auf einer Feature Seite)
 */
public class RelationColumn implements Comparable<RelationColumn> {
	private final List<Relation> relations = new ArrayList<>();
	private final Set<String> mustShowPath = new HashSet<>();
	
	public static TreeSet<RelationColumn> makeColumns(List<Relation> relations) {
		TreeSet<RelationColumn> cols = new TreeSet<>();
		relations.forEach(r -> find(r, cols));
	    return cols;
	}
	
	private static void find(Relation r, TreeSet<RelationColumn> cols) {
    	for (RelationColumn i : cols) {
    		if (i.getCol() == r.getColumn()) {
    			i.getRelations().add(r);
    			return;
    		}
    	}
		cols.add(new RelationColumn(r));
	}

	private RelationColumn(Relation r) {
		relations.add(r);
	}

	public List<Relation> getRelations() {
		return relations;
	}

	public int getCol() {
		return relations.get(0).getColumn();
	}

	@Override
	public int compareTo(RelationColumn o) {
		return Integer.valueOf(getCol()).compareTo(Integer.valueOf(o.getCol()));
	}
	
	public void findDuplicateTitles() {
		for (int i = 0; i < relations.size(); i++) {
			String ii = relations.get(i).getTitle();
			for (int j = 0; j < i; j++) {
				if (ii.equalsIgnoreCase(relations.get(j).getTitle())) {
					mustShowPath.add(relations.get(i).getId());
					mustShowPath.add(relations.get(j).getId());
				}
			}
		}
	}
	
	public boolean mustShowPath(String id) {
		return mustShowPath.contains(id);
	}
}
