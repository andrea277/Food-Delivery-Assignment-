import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
public class platform {
    private static platform instance;
    private static BigDecimal BASE_FEE = new BigDecimal("15");
    private static BigDecimal EXTRA_KM_FEE = new BigDecimal("3");
    private static BigDecimal SERVICE_RATE = new BigDecimal("0.10");
    private static int POOL_SIZE = 10;
    private static String DATA_PATH = "data";
    private Map<String, restaurant> restaurants;
    private Map<String, customer> customers;
    private Map<String, rider> riders;
    private Map<String, order> orders;
    private Map<String, promotion> promotions;
    private Set<String> cuisines;
    private PriorityQueue<order> readyOrders;
    private List<String> customerMessages;
    private List<String> auditRecords;
    private Map<String, order.status> riderDashboard;
    private platform() {
        restaurants = new LinkedHashMap<>();
        customers = new HashMap<>();
        riders = new HashMap<>();
        orders = new HashMap<>();
        promotions = new HashMap<>();
        cuisines = new LinkedHashSet<>();
        readyOrders = new PriorityQueue<>(Comparator.comparingInt((order x) -> x.getCustomer().getTier() == customer.tier.gold ? 0 : 1).thenComparing(order::getPlacedAt));
        customerMessages = new ArrayList<>();
        auditRecords = new ArrayList<>();
        riderDashboard = new HashMap<>();
    }
    public static platform getInstance() {
        if (instance == null) {
            instance = new platform();
        }
        return instance;
    }
    public void addRestaurant(restaurant value) {
        if (restaurants.containsKey(value.getId())) {
            throw new deliveryException(deliveryException.errorType.validation_error, "duplicate restaurant id");
        }
        restaurants.put(value.getId(), value);
        cuisines.addAll(value.getCuisines());
    }
    public void removeRestaurant(String id) {
        if (!restaurants.containsKey(id)) {
            throw new deliveryException(deliveryException.errorType.validation_error, "restaurant not found");
        }
        restaurants.remove(id);
        refreshCuisines();
    }
    private void refreshCuisines() {
        cuisines.clear();
        restaurants.values().stream().flatMap(x -> x.getCuisines().stream()).forEach(x -> cuisines.add(x));
    }
    public void addCustomer(customer value) {
        if (customers.containsKey(value.getId())) {
            throw new deliveryException(deliveryException.errorType.validation_error, "duplicate customer id");
        }
        customers.put(value.getId(), value);
    }
    public void addRider(rider value) {
        if (riders.containsKey(value.getId())) {
            throw new deliveryException(deliveryException.errorType.validation_error, "duplicate rider id");
        }
        riders.put(value.getId(), value);
    }
    public void addPromotion(promotion value) {
        String promo_code_one = value.getCode().toLowerCase();
        if (promotions.containsKey(promo_code_one)) {
            throw new deliveryException(deliveryException.errorType.validation_error, "duplicate promotion code");
        }
        promotions.put(promo_code_one, value);
    }
    public restaurant getRestaurant(String id) {
        restaurant value = restaurants.get(id);
        if (value == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "restaurant not found");
        }
        return value;
    }
    public customer getCustomer(String id) {
        customer value = customers.get(id);
        if (value == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "customer not found");
        }
        return value;
    }
    public rider getRider(String id) {
        rider value = riders.get(id);
        if (value == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "rider not found");
        }
        return value;
    }
    public order getOrder(String id) {
        order value = orders.get(id);
        if (value == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "order not found");
        }
        return value;
    }
    public promotion getPromotion(String code) {
        promotion value = promotions.get(code.toLowerCase());
        if (value == null) {
            throw new deliveryException(deliveryException.errorType.promotion_error, "promotion not found");
        }
        return value;
    }
    public List<restaurant> getRestaurants() {
        return Collections.unmodifiableList(new ArrayList<>(restaurants.values()));
    }
    public List<customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers.values()));
    }
    public List<rider> getRiders() {
        return Collections.unmodifiableList(new ArrayList<>(riders.values()));
    }
    public List<order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders.values()));
    }
    public Set<String> getCuisines() {
        return Collections.unmodifiableSet(cuisines);
    }
    public List<String> getCustomerMessages() {
        return Collections.unmodifiableList(customerMessages);
    }
    public List<String> getAuditRecords() {
        return Collections.unmodifiableList(auditRecords);
    }
    public Map<String, order.status> getRiderDashboard() {
        return Collections.unmodifiableMap(riderDashboard);
    }
    public int getPoolSize() {
        return POOL_SIZE;
    }
    public String getDataPath() {
        return DATA_PATH;
    }
    public List<restaurant> browse() {
        return restaurants.values().stream().filter(restaurant::isOpen).sorted(Comparator.comparingDouble(restaurant::getRating).reversed().thenComparing(restaurant::getName)).collect(Collectors.toList());
    }
    public List<restaurant> search(customer customer, String text) {
        customer.addSearch(text);
        String value = text.toLowerCase();
        return restaurants.values().stream().filter(restaurant::isOpen).filter(x -> x.getName().toLowerCase().contains(value) || x.getCuisines().stream().anyMatch(y -> y.toLowerCase().contains(value))).sorted(Comparator.comparingDouble(restaurant::getRating).reversed().thenComparing(restaurant::getName)).collect(Collectors.toList());
    }
    public List<restaurant> filter(customer customer, String district, String cuisine, Double minRating, BigDecimal maxPrice) {
        customer.addSearch("filter");
        return restaurants.values().stream().filter(restaurant::isOpen).filter(x -> district == null || district.isBlank() || x.getDistrict().equalsIgnoreCase(district)).filter(x -> cuisine == null || cuisine.isBlank() || x.getCuisines().stream().anyMatch(y -> y.equalsIgnoreCase(cuisine))).filter(x -> minRating == null || x.getRating() >= minRating).filter(x -> maxPrice == null || x.getMenu().stream().anyMatch(y -> y.getPrice(BigDecimal.ONE).compareTo(maxPrice) <= 0)).sorted(Comparator.comparingDouble(restaurant::getRating).reversed().thenComparing(restaurant::getName)).collect(Collectors.toList());
    }
    public void placeOrder(order value) {
        if (!value.getRestaurant().isOpen()) {
            throw new deliveryException(deliveryException.errorType.restaurant_closed, "restaurant is closed");
        }
        if (!value.getCustomer().ownsAddress(value.getAddress())) {
            throw new deliveryException(deliveryException.errorType.validation_error, "address does not belong to customer");
        }
        if (value.getLines().isEmpty()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "order must contain at least one line item");
        }
        for (order.lineItem line : value.getLines()) {
            menuItem item = line.getItem();
            if (!item.isAvailable()) {
                throw new deliveryException(deliveryException.errorType.item_unavailable, "item is unavailable");
            }
            if (item.getStock().compareTo(line.getQuantity()) < 0) {
                throw new deliveryException(deliveryException.errorType.stock_shortage, "not enough stock for " + item.getName());
            }
        }
        order.price finalPrice = value.calculatePrice();
        value.setFinalPrice(finalPrice);
        for (order.lineItem line : value.getLines()) {
            line.getItem().reduceStock(line.getQuantity());
        }
        value.addObserver((x, oldStatus, newStatus) -> customerMessages.add("customer notification: order " + x.getId() + " changed to " + newStatus));
        value.addObserver((x, oldStatus, newStatus) -> riderDashboard.put(x.getId(), newStatus));
        value.addObserver((x, oldStatus, newStatus) -> auditRecords.add("order " + x.getId() + " changed from " + oldStatus + " to " + newStatus));
        orders.put(value.getId(), value);
    }
    public void acceptOrder(String id) {
        getOrder(id).changeStatus(order.status.accepted);
    }
    public void rejectOrder(String id) {
        getOrder(id).cancel();
    }
    public void markPreparing(String id) {
        getOrder(id).changeStatus(order.status.preparing);
    }
    public void markReady(String id) {
        order value = getOrder(id);
        value.changeStatus(order.status.ready);
        readyOrders.offer(value);
    }
    public void assignNext(String riderId) {
        rider rider = getRider(riderId);
        if (!rider.isAvailable()) {
            throw new deliveryException(deliveryException.errorType.rider_busy, "rider is not available");
        }
        order selected = null;
        List<order> temp = new ArrayList<>();
        while (!readyOrders.isEmpty()) {
            order value = readyOrders.poll();
            if (selected == null && rider.getStrategy().canTake(value)) {
                selected = value;
            } else {
                temp.add(value);
            }
        }
        for (order value : temp) {
            readyOrders.offer(value);
        }
        if (selected == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "no suitable order is waiting");
        }
        rider.assign(selected);
    }
}