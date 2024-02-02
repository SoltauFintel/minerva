package minerva.seite;

import minerva.base.NlsString;

public interface IPostContentsData {

    NlsString getContent();
    
    NlsString getTitle();
    
    String getComment();
    
    void setDone(boolean done);
}
