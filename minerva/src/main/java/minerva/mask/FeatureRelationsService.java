package minerva.mask;

import java.util.ArrayList;
import java.util.List;

import github.soltaufintel.amalia.base.IdGenerator;
import minerva.model.BookSO;
import minerva.model.SeiteSO;

public class FeatureRelationsService {
    
    public List<Relation> getRelations(SeiteSO feature, FeatureFields ff) {
        List<Relation> relations = new ArrayList<>();
        BookSO book = feature.getBook();
        ff.getPages().forEach(id -> relations.add(new PageRelation(id, book)));
        ff.getTickets().forEach(ticket -> relations.add(new TicketRelation(ticket)));
        ff.getLinks().forEach(link -> relations.add(new LinkRelation(link)));
        relations.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        return relations;
    }
    
    public interface Relation {
        
        String getId();
        
        String getTitle();
        
        String getLink();
        
        String getIcon();
        
        void deleteFrom(FeatureFields ff);
    }
    
    public static class PageRelation implements Relation {
        private final String id;
        private final String title;
        private final String link;
        private final String icon;
        
        private PageRelation(String id, BookSO book) {
            this.id = id;
            SeiteSO seite = book.getWorkspace().findPage(id);
            link = seite == null ? "" : "/s/{branch}/" + seite.getBook().getBook().getFolder() + "/" + id;
            title = seite == null ? id : seite.getTitle();
            icon = seite.isFeatureTree() ? "fa-file fa-sitemap-color" : "fa-file-text greenbook";
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
            ff.getPages().remove(id);
        }
    }
    
    public static class TicketRelation implements Relation {
        private final String ticket;
        
        private TicketRelation(String ticket) {
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
            return "http://jira01.intern.x-map.de:8080/browse/" + ticket;
        }

        @Override
        public String getIcon() {
            return "fa-bookmark greenbook";
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
            return "fa-globe bluebook";
        }

        @Override
        public void deleteFrom(FeatureFields ff) {
            ff.getLinks().remove(link);
        }
    }
}
