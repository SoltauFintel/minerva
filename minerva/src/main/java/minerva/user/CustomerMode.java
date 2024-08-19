package minerva.user;

import minerva.base.StringService;

public class CustomerMode {
    private final String customer;

    public CustomerMode(String customer) {
        this.customer = customer;
    }

    public String getCustomer() {
        return customer;
    }

    public boolean isActive() {
        return !StringService.isNullOrEmpty(customer) && !"-".equals(customer);
    }
    
    @Override
    public String toString() {
        return customer == null ? "" : customer.toUpperCase();
    }
}
