package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class FeatureRelationsService {
    public static RelationsAdder relationsAdder = (feature, ff, relations) -> {};
    
    public List<Relation> getRelations(SeiteSO feature, FeatureFields ff) {
        List<Relation> relations = new ArrayList<>();
        BookSO book = feature.getBook();
        wegfuehrendeBeziehungen(ff, relations, book);
        ankommendeBeziehungen(feature, relations, book);
        relationsAdder.addRelations(feature, ff, relations);
        relations.sort((a, b) -> a.getSort().compareToIgnoreCase(b.getSort()));
        return relations;
    }

    private void wegfuehrendeBeziehungen(FeatureFields ff, List<Relation> relations, BookSO book) {
        ff.getPages().forEach(_id -> relations.add(new PageRelation(_id, book, i -> i.getPages().remove(_id))));
        ff.getLinks().forEach(link -> relations.add(new LinkRelation(link)));
    }

    private void ankommendeBeziehungen(SeiteSO feature, List<Relation> relations, BookSO book) {
        final String id = feature.getId();
        FeatureFieldsService sv2 = new FeatureFieldsService();
        for (SeiteSO as : book.getAlleSeiten()) {
            if (!id.equals(as.getId())) {
                FeatureFields ff2 = sv2.get(as);
                if (ff2.getPages().contains(id)) {
                    relations.add(new PageRelation(as.getId(), book, unused -> sv2.removeEntryAndSave(ff2, id, as)));
                }
            }
        }
    }

    public interface DeleteRoutine {
    
        void delete(FeatureFields ff);
    }
    
    public interface Relation {
        
        String getId();
        
        String getTitle();
        
        String getLink();
        
        String getIcon();
        
        int getColumn();
        
        String getColumnTitleKey();
        
        String getSort();
        
        default boolean isDeletable() {
        	return true;
        }
        
        void deleteFrom(FeatureFields ff);
        
        default boolean noBreak() {
            return false;
        }
    }
    
    public static class PageRelation implements Relation {
        private final String id;
        private final String title;
        private final String link;
        private final String icon;
        private final DeleteRoutine deleteRoutine;
        
        private PageRelation(String id, BookSO book, DeleteRoutine deleteRoutine) {
            this.id = id;
            SeiteSO seite = book.getWorkspace().findPage(id);
            link = seite == null ? "" : "/s/{branch}/" + seite.getBook().getBook().getFolder() + "/" + id;
            title = seite == null ? id : seite.getTitle();
            icon = seite.isFeatureTree() ? "fa-sitemap fa-sitemap-color" : "fa-file-text greenbook";
            this.deleteRoutine = deleteRoutine;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getLink() {
            return link;
        }

        @Override
        public String getIcon() {
            return icon + " fa-fw";
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            deleteRoutine.delete(ff);
        }

		@Override
		public int getColumn() {
			return 10;
		}

		@Override
		public String getColumnTitleKey() {
			return "frctPage";
		}

		@Override
		public String getSort() {
			return title;
		}
    }
    
    public static class LinkRelation implements Relation {
        private final String link;
        private final String id;
        private final String title;
        
        private LinkRelation(String link) {
            this.link = link == null ? "" : link;
            id = "link_" + IdGenerator.code6(link);
            title = WebpageTitleService.webpageTitleService.getTitle(link);
        }

        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getTitle() {
        	return title;
        }

        @Override
        public String getLink() {
            return link;
        }

        @Override
        public String getIcon() {
            return link.contains("atlassian.net/wiki/") ? "fa-file-text-o ftConfluenceLinkColor fa-fw" : "fa-globe ftLinkColor fa-fw";
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            ff.getLinks().remove(link);
        }

		@Override
		public int getColumn() {
			return 50;
		}

		@Override
		public String getColumnTitleKey() {
			return "frctLink";
		}

		@Override
		public String getSort() {
			return (link.contains("atlassian.net/wiki/") ? "1" : "2") + link;
		}
    }
    
    public interface RelationsAdder {
    	void addRelations(SeiteSO feature, FeatureFields ff, List<Relation> relations);
    }
}
