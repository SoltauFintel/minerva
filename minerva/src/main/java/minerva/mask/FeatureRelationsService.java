package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.config.MinervaOptions;
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
        relations.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        return relations;
    }

    private void wegfuehrendeBeziehungen(FeatureFields ff, List<Relation> relations, BookSO book) {
        ff.getPages().forEach(_id -> relations.add(new PageRelation(_id, book, i -> i.getPages().remove(_id))));
        ff.getTickets().forEach(ticket -> relations.add(new TicketRelation(ticket)));
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
        
        default boolean isDeletable() {
        	return true;
        }
        
        void deleteFrom(FeatureFields ff);
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
            return icon;
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            deleteRoutine.delete(ff);
        }
    }
    
    public static class TicketRelation implements Relation {
        private final String ticket;
        
        public TicketRelation(String ticket) {
            this.ticket = ticket;
        }
        
        @Override
        public String getId() {
            return ticket;
        }
        
        @Override
        public String getTitle() {
            return ticket;
        }

        @Override
        public String getLink() {
			return "https://" + MinervaOptions.JIRA_CUSTOMER.get() + ".atlassian.net/browse/" + ticket;
        }

        @Override
        public String getIcon() {
            return "fa-bookmark ticket1";
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            ff.getTickets().remove(ticket);
        }
    }
    
    public static class LinkRelation implements Relation {
        private final String link;
        private final String id;
        
        private LinkRelation(String link) {
            this.link = link;
            id = "link_" + IdGenerator.code6(link);
        }

        @Override
        public String getId() {
            return id;
        }
        
        @Override
        public String getTitle() {
            return link;
        }

        @Override
        public String getLink() {
            return link;
        }

        @Override
        public String getIcon() {
            return link != null && link.contains("atlassian.net/wiki/") ? "fa-file-text-o ftConfluenceLinkColor" : "fa-globe ftLinkColor";
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            ff.getLinks().remove(link);
        }
    }
    
    public interface RelationsAdder {
    	void addRelations(SeiteSO feature, FeatureFields ff, List<Relation> relations);
    }
}
