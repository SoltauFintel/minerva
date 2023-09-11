package minerva.model;

public interface ISeite {
    
    String getId();

    String getTitle();

    boolean isSorted();
    
    boolean isReversedOrder();
    
    SeitenSO getSeiten();
}
