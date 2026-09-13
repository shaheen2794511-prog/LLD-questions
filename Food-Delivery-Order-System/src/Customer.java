public class Customer {

    private final String customerId;
    private final String name;
    private final String address;
    private final boolean premium;

    public Customer(
            String customerId,
            String name,
            String address,
            boolean premium) {

        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.premium = premium;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isPremium() {
        return premium;
    }
}
