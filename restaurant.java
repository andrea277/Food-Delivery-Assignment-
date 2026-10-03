import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
public class restaurant {
    private String id;
    private String name;
    private String district;
    private List<String> cuisines;
    private double rating;
    private boolean open;
    private Map<String, menuItem> menu;
    private int completedOrders;
    public restaurant(String id, String name, String district, List<String> cuisines, double rating) {
        if (id == null || id.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "restaurant id is required");
        }
        if (name == null || name.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "restaurant name is required");
        }
        if (rating < 0 || rating > 5) {
            throw new deliveryException(deliveryException.errorType.validation_error, "rating must be between zero and five");
        }
        this.id = id;
        this.name = name;
        this.district = district;
        this.cuisines = new ArrayList<>(cuisines);
        this.rating = rating;
        this.open = true;
        this.menu = new LinkedHashMap<>();
        this.completedOrders = 0;
    }
    public void addMenuItem(menuItem item) {
        if (item == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "item is required");
        }
        if (menu.containsKey(item.getId())) {
            throw new deliveryException(deliveryException.errorType.validation_error, "duplicate menu item");
        }
        item.setRestaurantId(id);
        menu.put(item.getId(), item);
    }
    public void removeMenuItem(String itemId) {
        if (!menu.containsKey(itemId)) {
            throw new deliveryException(deliveryException.errorType.validation_error, "menu item not found");
        }
        menu.remove(itemId);
    }
    public menuItem getMenuItem(String itemId) {
        menuItem item = menu.get(itemId);
        if (item == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "menu item not found");
        }
        return item;
    }
    public List<menuItem> getMenu() {
        return Collections.unmodifiableList(new ArrayList<>(menu.values()));
    }
    public void setOpen(boolean value) {
        open = value;
    }
    public void addCompletedOrder() {
        completedOrders++;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDistrict() {
        return district;
    }
    public List<String> getCuisines() {
        return Collections.unmodifiableList(cuisines);
    }
    public double getRating() {
        return rating;
    }
    public boolean isOpen() {
        return open;
    }
    public int getCompletedOrders() {
        return completedOrders;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof restaurant)) return false;
        restaurant other = (restaurant) obj;
        return Objects.equals(id, other.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        return id + " " + name + " " + district + " " + rating;
    }
}