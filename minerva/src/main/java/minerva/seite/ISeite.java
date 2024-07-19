package minerva.seite;

import minerva.model.SeitenSO;

public interface ISeite {
    
    String getId();

    String getTitle();

    boolean isSorted();
    
    boolean isReversedOrder();
    
    SeitenSO getSeiten();
}
