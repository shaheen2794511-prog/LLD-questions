
public class FoodDeliveryDemo {

    public static void main(String[] args) {

        // 1. Create customer
        Customer customer = new Customer(
            "C101", "Aarav", "Delhi", true
        );

        // 2. Create restaurant and menu
        Restaurant restaurant =
            new Restaurant("R101", "Food Hub");

        FoodItem pizza =
            new FoodItem("F101", "Pizza", 300, true);

        FoodItem burger =
            new FoodItem("F102", "Burger", 150, true);

        restaurant.addItem(pizza);
        restaurant.addItem(burger);

        // 3. Create order using Builder pattern
        Order order = new Order.Builder(
            "O101", customer, restaurant
        )
            .addItem(restaurant.getAvailableItem("F101"), 2)
            .addItem(restaurant.getAvailableItem("F102"), 1)
            .build();

        // 4. Register Observer for notifications
        order.addObserver(new NotificationObserver(
            List.of(new EmailChannel(), new SmsChannel())
        ));

        // 5. Configure validation chain
        OrderValidator validator =
            new NonEmptyOrderValidator();

        validator.setNext(
            new ItemAvailabilityValidator()
        );

        // 6. Configure pricing
        PricingService pricing = new PricingService(List.of(
            new FlatDeliveryFee(50),
            new PremiumDiscount(),
            new TaxRule(0.05)
        ));

        // 7. Create payment and facade
        PaymentService paymentService = new PaymentService();

        OrderFacade facade = new OrderFacade(
            validator, pricing, paymentService
        );

        PaymentStrategy payment =
            PaymentFactory.create(PaymentMethod.UPI);

        // 8. Place order using Command pattern
        OrderCommand placeOrder =
            new PlaceOrderCommand(facade, order, payment);

        placeOrder.execute();

        // 9. Update order lifecycle
        order.startPreparing();
        order.markReady();

        // 10. Assign delivery partner
        List<DeliveryPartner> partners = List.of(
            new DeliveryPartner("D101", "Ravi", 12.0),
            new DeliveryPartner("D102", "Simran", 10.5)
        );

        DeliveryService delivery = new DeliveryService(
            new NearestPartner()
        );

        delivery.assign(order, partners);

        // 11. Deliver the order
        order.markOutForDelivery();
        order.deliver();

        // 12. Print final result
        System.out.println("\n===== ORDER SUMMARY =====");
        System.out.println("Order ID: " + order.getId());
        System.out.println("Restaurant: " +
            order.getRestaurant().getName());
        System.out.println("Total Amount: ₹" + order.getTotal());
        System.out.println("Order Status: " + order.getStatus());
        System.out.println("Delivery Partner: " +
            order.getPartner().getName());
    }
}
