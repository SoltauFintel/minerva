package minerva.task;

public interface Task {

    String getId();
    
    String getLogin();
    
    String getPerson();
    
    String getDateTime();
    
    String getText();
    
    String getLink();
    
    String getParentLink();
    
    String getParentTitle();
    
    /**
     * @return RB key
     */
    String getTypeName();
    
    String getColor();
}
