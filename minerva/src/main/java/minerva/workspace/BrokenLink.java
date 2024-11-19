package minerva.workspace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import minerva.workspace.BrokenLinksService.BLCaller;

public class BrokenLink {
    private final String customer;
    private final String errorType;
    private final String url;
    /** key: caller ID */
    private final Map<String, List<BLCaller>> callers = new HashMap<>();

    public BrokenLink(String customer, String errorType, String url) {
        this.customer = customer;
        this.errorType = errorType;
        this.url = url;
    }

    public String getCustomer() {
        return customer;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, List<BLCaller>> getCallers() {
        return callers;
    }
}
