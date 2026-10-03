import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
public class reports {
    private platform platform;
    public reports(platform platform) {
        this.platform = platform;
    }
    public BigDecimal totalRevenue(LocalDate start, LocalDate end) {
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        return platform.getOrders().stream().filter(x -> x.getStatus() == order.status.delivered).filter(x -> !x.getPlacedAt().isBefore(startTime)).filter(x -> x.getPlacedAt().isBefore(endTime)).map(order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public List<Map.Entry<String, BigDecimal>> topFiveRestaurants(int year, int month) {
        return platform.getOrders().stream().filter(x -> x.getStatus() == order.status.delivered).filter(x -> x.getPlacedAt().getYear() == year).filter(x -> x.getPlacedAt().getMonthValue() == month).collect(Collectors.groupingBy(x -> x.getRestaurant().getName(), Collectors.reducing(BigDecimal.ZERO, order::getTotal, BigDecimal::add))).entrySet().stream().sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed().thenComparing(Map.Entry::getKey)).limit(5).collect(Collectors.toList());
    }
    public Map<String, Double> averageOrderValueByDistrict() {
        return platform.getOrders().stream().collect(Collectors.groupingBy(x -> x.getAddress().getDistrict(), Collectors.averagingDouble(x -> x.getTotal().doubleValue())));
    }
    public List<restaurant> highRatedRestaurants() {
        return platform.getRestaurants().stream().filter(x -> x.getRating() > 4.5).filter(x -> x.getCompletedOrders() >= 20).collect(Collectors.toList());
    }
    public Map<order.status, Long> orderCountByStatus() {
        return platform.getOrders().stream().collect(Collectors.groupingBy(order::getStatus, Collectors.counting()));
    }
    public List<String> riderStats() {
        return platform.getRiders().stream().sorted(Comparator.comparingInt(rider::getCompletedDeliveries).reversed().thenComparing(rider::getName)).map(x -> {
            List<order> riderOrders = platform.getOrders().stream().filter(y -> x.getId().equals(y.getRiderId())).filter(y -> y.getStatus() == order.status.delivered).collect(Collectors.toList());
            double average = riderOrders.stream().mapToLong(order::getDeliveryMinutes).average().orElse(0);
            return x.getName() + " deliveries: " + x.getCompletedDeliveries() + " average duration: " + average + " minutes";
        }).collect(Collectors.toList());
    }
    public Optional<String> mostOrderedItem() {
        return platform.getOrders().stream().flatMap(x -> x.getLines().stream()).collect(Collectors.groupingBy(x -> x.getItem().getName(), Collectors.summingDouble(x -> x.getQuantity().doubleValue()))).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
    public List<order> customerHistory(String customerId) {
        return platform.getOrders().stream().filter(x -> x.getCustomer().getId().equals(customerId)).sorted(Comparator.comparing(order::getPlacedAt).reversed()).collect(Collectors.toList());
    }
    public BigDecimal customerSpent(String customerId) {
        return customerHistory(customerId).stream().map(order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public Optional<Integer> peakOrderingHour() {
        return platform.getOrders().stream().collect(Collectors.groupingBy(x -> x.getPlacedAt().getHour(), Collectors.counting())).entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
    public List<customer> inactiveCustomers() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        return platform.getCustomers().stream().filter(customer -> platform.getOrders().stream().filter(x -> x.getCustomer().getId().equals(customer.getId())).map(order::getPlacedAt).max(LocalDateTime::compareTo).map(x -> x.isBefore(limit)).orElse(true)).collect(Collectors.toList());
    }
    public List<order> todayRestaurantOrders(restaurant restaurant) {
        LocalDate today = LocalDate.now();
        return platform.getOrders().stream().filter(x -> x.getRestaurant().equals(restaurant)).filter(x -> x.getPlacedAt().toLocalDate().equals(today)).sorted(Comparator.comparing(order::getPlacedAt)).collect(Collectors.toList());
    }
    public BigDecimal todayRestaurantRevenue(restaurant restaurant) {
        return todayRestaurantOrders(restaurant).stream().filter(x -> x.getStatus() == order.status.delivered).map(order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}