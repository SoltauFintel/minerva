package minerva.workspace;

import java.util.ArrayList;
import java.util.List;

public class BrokenLink {
    private final String customer;
    private final String errorType;
    private final String url;
    private final List<String> callers = new ArrayList<>();

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

    public List<String> getCallers() {
        return callers;
    }
}
