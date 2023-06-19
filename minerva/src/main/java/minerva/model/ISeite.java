package minerva.model;

public interface ISeite {
    
    String getId();

    String getTitle();

    boolean isSorted();
    
    SeitenSO getSeiten();
}
