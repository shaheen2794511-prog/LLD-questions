import java.util.*;
import java.util.function.ToDoubleBiFunction;

// ===========================================================================
// EXCEPTIONS
// ===========================================================================

class OrderException extends RuntimeException {
    OrderException(String message) { super(message); }
}

class InvalidOrderTransitionException extends OrderException {
    InvalidOrderTransitionException(String message) { super(message); }
}

class ItemUnavailableException extends OrderException {
    ItemUnavailableException(String message) { super(message); }
}

class PaymentRequiredException extends OrderException {
    PaymentRequiredException(String message) { super(message); }
}

class NoDeliveryPartnerAvailableException extends OrderException {
    NoDeliveryPartnerAvailableException(String message) { super(message); }
}

// ===========================================================================
// DOMAIN MODELS
// ===========================================================================

enum OrderStatus {
    CREATED, CONFIRMED, PREPARING, READY_FOR_PICKUP, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
}

final class TransitionTable {
    // Cancellation allowed up to READY_FOR_PICKUP (before the delivery
    // partner actually picks the order up); disallowed afterwards.
    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);
    static {
        ALLOWED.put(OrderStatus.CREATED, List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.CONFIRMED, List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.PREPARING, List.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.READY_FOR_PICKUP, List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED));
        ALLOWED.put(OrderStatus.OUT_FOR_DELIVERY, List.of(OrderStatus.DELIVERED));
        ALLOWED.put(OrderStatus.DELIVERED, List.of());
        ALLOWED.put(OrderStatus.CANCELLED, List.of());
    }
    static boolean canTransition(OrderStatus from, OrderStatus to) {
        return ALLOWED.get(from).contains(to);
    }
    private TransitionTable() {}
}

class Customer {
    final String customerId;
    final String name;
    final String address;
    final boolean premium;

    Customer(String customerId, String name, String address, boolean premium) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.premium = premium;
    }
}

class FoodItem {
    final String itemId;
    final String name;
    final double price;
    boolean available;

    FoodItem(String itemId, String name, double price) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.available = true;
    }
}

class Restaurant {
    final String restaurantId;
    final String name;
    final String address;
    private final Map<String, FoodItem> menu = new LinkedHashMap<>();

    Restaurant(String restaurantId, String name, String address) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
    }

    void addMenuItem(FoodItem item) { menu.put(item.itemId, item); }

    FoodItem getItem(String itemId) {
        FoodItem item = menu.get(itemId);
        if (item == null) {
            throw new ItemUnavailableException("Item " + itemId + " is not on " + name + "'s menu");
        }
        return item;
    }

    void setItemAvailability(String itemId, boolean available) {
        getItem(itemId).available = available;
    }

    boolean canFulfil(List<OrderItem> items) {
        for (OrderItem oi : items) {
            if (!oi.foodItem.available) return false;
        }
        return true;
    }
}

class OrderItem {
    final FoodItem foodItem;
    final int quantity;

    OrderItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    double subtotal() { return foodItem.price * quantity; }
}

class DeliveryPartner {
    final String partnerId;
    final String name;
    String currentLocation;
    double rating;
    boolean available = true;
    int activeOrdersCount = 0;

    DeliveryPartner(String partnerId, String name, String currentLocation, double rating) {
        this.partnerId = partnerId;
        this.name = name;
        this.currentLocation = currentLocation;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "DeliveryPartner{" + name + ", rating=" + rating + ", active=" + activeOrdersCount + "}";
    }
}

class PaymentResult {
    final boolean success;
    final String transactionId;
    final String message;

    PaymentResult(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }
}

// An observer interface for anything that wants to react to order events.
// Notifications are ONE implementation of this - not the only possible one
// (analytics, audit-logging, SLA timers could subscribe the same way).
interface OrderObserver {
    void onOrderEvent(Order order, OrderStatus event);
}

class Order {
    final String orderId;
    final Customer customer;
    final Restaurant restaurant;
    final List<OrderItem> items;
    OrderStatus status = OrderStatus.CREATED;
    double totalAmount = 0.0;
    PaymentResult paymentResult;
    DeliveryPartner deliveryPartner;
    private final List<OrderObserver> observers = new ArrayList<>();

    Order(String orderId, Customer customer, Restaurant restaurant, List<OrderItem> items) {
        this.orderId = orderId;
        this.customer = customer;
        this.restaurant = restaurant;
        this.items = items;
    }

    void addObserver(OrderObserver observer) { observers.add(observer); }

    private void notifyObservers(OrderStatus event) {
        for (OrderObserver observer : observers) observer.onOrderEvent(this, event);
    }

    boolean itemsAvailable() { return restaurant.canFulfil(items); }

    void transitionTo(OrderStatus newStatus) {
        if (!TransitionTable.canTransition(status, newStatus)) {
            throw new InvalidOrderTransitionException(
                "Cannot move order " + orderId + " from " + status + " to " + newStatus);
        }
        status = newStatus;
        notifyObservers(newStatus);
    }

    @Override
    public String toString() {
        return "Order{" + orderId + ", status=" + status + ", total=" + totalAmount + "}";
    }
}

// ===========================================================================
// PAYMENT  (Strategy pattern -> Open/Closed for new payment methods)
// ===========================================================================

interface PaymentStrategy {
    PaymentResult pay(double amount);
}

class CreditCardPayment implements PaymentStrategy {
    private final String cardNumber;
    CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }

    @Override
    public PaymentResult pay(double amount) {
        String masked = "****" + cardNumber.substring(cardNumber.length() - 4);
        String txnId = "CC-" + Math.abs(Objects.hash(masked, amount)) % 100000000;
        return new PaymentResult(true, txnId, "Charged Rs." + amount + " to card " + masked);
    }
}

class UpiPayment implements PaymentStrategy {
    private final String upiId;
    UpiPayment(String upiId) { this.upiId = upiId; }

    @Override
    public PaymentResult pay(double amount) {
        String txnId = "UPI-" + Math.abs(Objects.hash(upiId, amount)) % 100000000;
        return new PaymentResult(true, txnId, "Collected Rs." + amount + " via UPI (" + upiId + ")");
    }
}

class CashOnDeliveryPayment implements PaymentStrategy {
    // Cash is physically collected at delivery time, but the business still
    // needs to know payment is "arranged" before CONFIRMED - so COD counts
    // as a successful reservation of payment upfront.
    @Override
    public PaymentResult pay(double amount) {
        return new PaymentResult(true, null, "Rs." + amount + " to be collected on delivery");
    }
}

class PaymentService {
    // Thin wrapper so OrderService never talks to a PaymentStrategy directly.
    PaymentResult processPayment(Order order, PaymentStrategy strategy) {
        PaymentResult result = strategy.pay(order.totalAmount);
        order.paymentResult = result;
        return result;
    }
}

// ===========================================================================
// PRICING  (Strategy + Composite -> new pricing/discount rules plug in freely)
// ===========================================================================

interface PricingRule {
    // Each rule receives the order and running total so far, and returns the
    // new running total. Rules are composed in a list/pipeline, so adding a
    // new rule never requires touching existing ones.
    double apply(Order order, double runningTotal);
}

class FoodSubtotalRule implements PricingRule {
    @Override
    public double apply(Order order, double runningTotal) {
        double total = 0.0;
        for (OrderItem oi : order.items) total += oi.subtotal();
        return total;
    }
}

class FlatDeliveryFeeRule implements PricingRule {
    private final double fee;
    FlatDeliveryFeeRule(double fee) { this.fee = fee; }

    @Override
    public double apply(Order order, double runningTotal) { return runningTotal + fee; }
}

class FreeDeliveryAboveThresholdRule implements PricingRule {
    private final double threshold;
    private final double fee;
    FreeDeliveryAboveThresholdRule(double threshold, double fee) {
        this.threshold = threshold;
        this.fee = fee;
    }

    @Override
    public double apply(Order order, double runningTotal) {
        double foodSubtotal = 0.0;
        for (OrderItem oi : order.items) foodSubtotal += oi.subtotal();
        return foodSubtotal >= threshold ? runningTotal : runningTotal + fee;
    }
}

class PercentageTaxRule implements PricingRule {
    private final double percentage;
    PercentageTaxRule(double percentage) { this.percentage = percentage; }

    @Override
    public double apply(Order order, double runningTotal) { return runningTotal + runningTotal * percentage; }
}

class PremiumCustomerDiscountRule implements PricingRule {
    private final double discountPct;
    PremiumCustomerDiscountRule(double discountPct) { this.discountPct = discountPct; }

    @Override
    public double apply(Order order, double runningTotal) {
        return order.customer.premium ? runningTotal * (1 - discountPct) : runningTotal;
    }
}

class RestaurantOfferRule implements PricingRule {
    // Example of a restaurant-specific offer - flat amount off, only for a
    // given restaurant id. Shows how new, narrowly-scoped rules can be added
    // later without modifying anything above.
    private final String restaurantId;
    private final double flatOff;
    RestaurantOfferRule(String restaurantId, double flatOff) {
        this.restaurantId = restaurantId;
        this.flatOff = flatOff;
    }

    @Override
    public double apply(Order order, double runningTotal) {
        if (order.restaurant.restaurantId.equals(restaurantId)) {
            return Math.max(0.0, runningTotal - flatOff);
        }
        return runningTotal;
    }
}

class PricingEngine {
    private final List<PricingRule> rules;
    PricingEngine(List<PricingRule> rules) { this.rules = rules; }

    double calculate(Order order) {
        double total = 0.0;
        for (PricingRule rule : rules) total = rule.apply(order, total);
        return Math.round(total * 100.0) / 100.0;
    }
}

// ===========================================================================
// DELIVERY ASSIGNMENT  (Strategy -> business can swap assignment algorithms)
// ===========================================================================

interface DeliveryAssignmentStrategy {
    DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order);
}

class AnyAvailablePartnerStrategy implements DeliveryAssignmentStrategy {
    @Override
    public DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order) {
        return availablePartners.isEmpty() ? null : availablePartners.get(0);
    }
}

class HighestRatedPartnerStrategy implements DeliveryAssignmentStrategy {
    @Override
    public DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order) {
        return availablePartners.stream().max(Comparator.comparingDouble(p -> p.rating)).orElse(null);
    }
}

class LeastActiveOrdersStrategy implements DeliveryAssignmentStrategy {
    @Override
    public DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order) {
        return availablePartners.stream().min(Comparator.comparingInt(p -> p.activeOrdersCount)).orElse(null);
    }
}

class NearestPartnerStrategy implements DeliveryAssignmentStrategy {
    // Distance function is injected so this stays testable without a real map API.
    private final ToDoubleBiFunction<String, String> distanceFn;
    NearestPartnerStrategy(ToDoubleBiFunction<String, String> distanceFn) { this.distanceFn = distanceFn; }

    @Override
    public DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order) {
        String dest = order.restaurant.address;
        return availablePartners.stream()
                .min(Comparator.comparingDouble(p -> distanceFn.applyAsDouble(p.currentLocation, dest)))
                .orElse(null);
    }
}

class PremiumCustomerPriorityStrategy implements DeliveryAssignmentStrategy {
    // Decorator over another strategy: premium customers always get the
    // highest-rated partner; everyone else falls back to the wrapped strategy.
    private final DeliveryAssignmentStrategy fallback;
    private final DeliveryAssignmentStrategy premiumStrategy = new HighestRatedPartnerStrategy();

    PremiumCustomerPriorityStrategy(DeliveryAssignmentStrategy fallback) { this.fallback = fallback; }

    @Override
    public DeliveryPartner selectPartner(List<DeliveryPartner> availablePartners, Order order) {
        if (order.customer.premium) return premiumStrategy.selectPartner(availablePartners, order);
        return fallback.selectPartner(availablePartners, order);
    }
}

class DeliveryService {
    private DeliveryAssignmentStrategy strategy;
    private final List<DeliveryPartner> partners = new ArrayList<>();

    DeliveryService(DeliveryAssignmentStrategy strategy) { this.strategy = strategy; }

    void registerPartner(DeliveryPartner partner) { partners.add(partner); }

    void setStrategy(DeliveryAssignmentStrategy strategy) { this.strategy = strategy; }

    DeliveryPartner assignPartner(Order order) {
        List<DeliveryPartner> available = partners.stream().filter(p -> p.available).toList();
        DeliveryPartner partner = strategy.selectPartner(available, order);
        if (partner == null) {
            throw new NoDeliveryPartnerAvailableException("No delivery partner currently available");
        }
        partner.available = false;
        partner.activeOrdersCount++;
        order.deliveryPartner = partner;
        return partner;
    }

    void releasePartner(DeliveryPartner partner) {
        partner.available = true;
        partner.activeOrdersCount = Math.max(0, partner.activeOrdersCount - 1);
    }
}

// ===========================================================================
// NOTIFICATIONS  (Strategy for channels + Observer for order events)
// ===========================================================================

interface NotificationChannel {
    void send(Customer customer, String message);
}

class EmailNotificationChannel implements NotificationChannel {
    @Override
    public void send(Customer customer, String message) {
        System.out.println("  [EMAIL -> " + customer.name + "] " + message);
    }
}

class SmsNotificationChannel implements NotificationChannel {
    @Override
    public void send(Customer customer, String message) {
        System.out.println("  [SMS   -> " + customer.name + "] " + message);
    }
}

class PushNotificationChannel implements NotificationChannel {
    @Override
    public void send(Customer customer, String message) {
        System.out.println("  [PUSH  -> " + customer.name + "] " + message);
    }
}

class NotificationService implements OrderObserver {
    // Subscribes to Order events and fans them out across all configured
    // channels. Adding a new channel = new NotificationChannel class + adding
    // it to this list; adding a new event message = one map entry.
    private static final Map<OrderStatus, String> MESSAGES = new EnumMap<>(OrderStatus.class);
    static {
        MESSAGES.put(OrderStatus.CONFIRMED, "Your order %s has been confirmed!");
        MESSAGES.put(OrderStatus.PREPARING, "Your order %s is being prepared.");
        MESSAGES.put(OrderStatus.READY_FOR_PICKUP, "Your order %s is ready for pickup.");
        MESSAGES.put(OrderStatus.OUT_FOR_DELIVERY, "Your order %s is out for delivery.");
        MESSAGES.put(OrderStatus.DELIVERED, "Your order %s has been delivered. Enjoy!");
        MESSAGES.put(OrderStatus.CANCELLED, "Your order %s has been cancelled.");
    }

    private final List<NotificationChannel> channels;
    NotificationService(List<NotificationChannel> channels) { this.channels = channels; }

    @Override
    public void onOrderEvent(Order order, OrderStatus event) {
        String template = MESSAGES.getOrDefault(event, "Order %s status: " + event);
        String message = String.format(template, order.orderId);
        for (NotificationChannel channel : channels) channel.send(order.customer, message);
    }
}

// ===========================================================================
// ORDER SERVICE  (Facade / orchestrator - delegates, doesn't implement logic)
// ===========================================================================

class OrderService {
    // Coordinates the use cases requested by clients (e.g. an API layer).
    // Every actual algorithm lives in a collaborator that was injected in,
    // so OrderService itself rarely needs to change - only the wiring does.
    private final PricingEngine pricingEngine;
    private final PaymentService paymentService;
    private final DeliveryService deliveryService;
    private int idCounter = 1;

    OrderService(PricingEngine pricingEngine, PaymentService paymentService, DeliveryService deliveryService) {
        this.pricingEngine = pricingEngine;
        this.paymentService = paymentService;
        this.deliveryService = deliveryService;
    }

    Order placeOrder(Customer customer, Restaurant restaurant, Map<String, Integer> itemQuantities,
                      List<OrderObserver> observers) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            orderItems.add(new OrderItem(restaurant.getItem(entry.getKey()), entry.getValue()));
        }
        String orderId = String.format("ORD-%04d", idCounter++);
        Order order = new Order(orderId, customer, restaurant, orderItems);
        if (observers != null) for (OrderObserver o : observers) order.addObserver(o);
        order.totalAmount = pricingEngine.calculate(order);
        return order;
    }

    PaymentResult makePayment(Order order, PaymentStrategy strategy) {
        return paymentService.processPayment(order, strategy);
    }

    void confirmOrder(Order order) {
        if (!order.itemsAvailable()) {
            throw new ItemUnavailableException("One or more items in " + order.orderId + " are unavailable");
        }
        if (order.paymentResult == null || !order.paymentResult.success) {
            throw new PaymentRequiredException("Payment must succeed before confirming " + order.orderId);
        }
        order.transitionTo(OrderStatus.CONFIRMED);
    }

    void updateStatus(Order order, OrderStatus newStatus) {
        order.transitionTo(newStatus);
    }

    DeliveryPartner assignDeliveryPartner(Order order) {
        if (order.status != OrderStatus.READY_FOR_PICKUP) {
            throw new InvalidOrderTransitionException(
                "Delivery partner can only be assigned once the order is READY_FOR_PICKUP");
        }
        return deliveryService.assignPartner(order);
    }

    void cancelOrder(Order order) {
        order.transitionTo(OrderStatus.CANCELLED);
    }
}

// ===========================================================================
// DEMONSTRATION
// ===========================================================================

public class FoodDeliverySystem {
    public static void main(String[] args) {
        // ---- Set up a restaurant and its menu -----------------------------
        Restaurant restaurant = new Restaurant("R1", "Spice Villa", "MG Road, Lucknow");
        restaurant.addMenuItem(new FoodItem("I1", "Paneer Butter Masala", 240.0));
        restaurant.addMenuItem(new FoodItem("I2", "Butter Naan", 40.0));
        restaurant.addMenuItem(new FoodItem("I3", "Gulab Jamun", 90.0));

        // ---- Customers ------------------------------------------------------
        Customer priya = new Customer("C1", "Priya", "Hazratganj, Lucknow", true);

        // ---- Delivery partners ----------------------------------------------
        DeliveryService deliveryService = new DeliveryService(new LeastActiveOrdersStrategy());
        deliveryService.registerPartner(new DeliveryPartner("D1", "Arjun", "Hazratganj", 4.2));
        deliveryService.registerPartner(new DeliveryPartner("D2", "Kabir", "Gomti Nagar", 4.9));
        // Premium customers should get the best-rated partner -> wrap the strategy.
        deliveryService.setStrategy(new PremiumCustomerPriorityStrategy(new LeastActiveOrdersStrategy()));

        // ---- Pricing rules (order matters: subtotal -> delivery -> tax -> discount)
        PricingEngine pricingEngine = new PricingEngine(List.of(
                new FoodSubtotalRule(),
                new FreeDeliveryAboveThresholdRule(500, 50),
                new PercentageTaxRule(0.05),
                new PremiumCustomerDiscountRule(0.10)
        ));

        // ---- Notifications ----------------------------------------------------
        NotificationService notifications = new NotificationService(List.of(
                new EmailNotificationChannel(),
                new SmsNotificationChannel()
        ));

        // ---- Services & Facade --------------------------------------------
        PaymentService paymentService = new PaymentService();
        OrderService orderService = new OrderService(pricingEngine, paymentService, deliveryService);

        // =====================================================================
        System.out.println("1) PLACE ORDER");
        Map<String, Integer> items1 = new LinkedHashMap<>();
        items1.put("I1", 2);
        items1.put("I2", 3);
        items1.put("I3", 1);
        Order order = orderService.placeOrder(priya, restaurant, items1, List.of(notifications));
        System.out.println("   " + order + "  (2xPBM + 3xNaan + 1xGulabJamun)");

        System.out.println("\n2) PAY FOR ORDER (UPI)");
        PaymentResult result = orderService.makePayment(order, new UpiPayment("priya@upi"));
        System.out.println("   Payment result: success=" + result.success + ", msg='" + result.message + "'");

        System.out.println("\n3) CONFIRM ORDER");
        orderService.confirmOrder(order);
        System.out.println("   " + order);

        System.out.println("\n4) MOVE THROUGH PREPARATION");
        orderService.updateStatus(order, OrderStatus.PREPARING);
        orderService.updateStatus(order, OrderStatus.READY_FOR_PICKUP);
        System.out.println("   " + order);

        System.out.println("\n5) ASSIGN DELIVERY PARTNER (premium customer -> highest rated)");
        DeliveryPartner partner = orderService.assignDeliveryPartner(order);
        System.out.println("   Assigned: " + partner);

        System.out.println("\n6) OUT FOR DELIVERY -> DELIVERED");
        orderService.updateStatus(order, OrderStatus.OUT_FOR_DELIVERY);
        orderService.updateStatus(order, OrderStatus.DELIVERED);

        System.out.println("\n7) TRY TO CANCEL A DELIVERED ORDER (should fail)");
        try {
            orderService.cancelOrder(order);
        } catch (InvalidOrderTransitionException e) {
            System.out.println("   Rejected as expected -> " + e.getMessage());
        }

        System.out.println("\n8) A SECOND ORDER THAT GETS CANCELLED BEFORE PICKUP");
        Map<String, Integer> items2 = new LinkedHashMap<>();
        items2.put("I2", 1);
        Order order2 = orderService.placeOrder(priya, restaurant, items2, List.of(notifications));
        orderService.makePayment(order2, new CashOnDeliveryPayment());
        orderService.confirmOrder(order2);
        orderService.updateStatus(order2, OrderStatus.PREPARING);
        orderService.cancelOrder(order2);
        System.out.println("   " + order2);

        System.out.println("\n9) TRYING TO CONFIRM AN ORDER WITH AN UNAVAILABLE ITEM");
        restaurant.setItemAvailability("I3", false);
        Map<String, Integer> items3 = new LinkedHashMap<>();
        items3.put("I3", 1);
        Order order3 = orderService.placeOrder(priya, restaurant, items3, List.of(notifications));
        orderService.makePayment(order3, new CreditCardPayment("4111111111111111"));
        try {
            orderService.confirmOrder(order3);
        } catch (ItemUnavailableException e) {
            System.out.println("   Rejected as expected -> " + e.getMessage());
        }
    }
}
