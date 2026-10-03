import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
public abstract class menuItem {
    protected String id;
    protected String name;
    protected String category;
    protected int preperationTime;
    protected boolean avaliable;
    protected BigDecimal stock;
    protected String restaurantId;
    public menuItem(String id, String name, String category, int preperationTime, BigDecimal stock) {
        if (id == null || id.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "item id is required");
        }
        if (name == null || name.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "item name is required");
        }
        if (preperationTime <= 0) {
            throw new deliveryException(deliveryException.errorType.validation_error, "preparation time must be greater than zero");
        }
        if (stock == null || stock.compareTo(BigDecimal.ZERO) < 0) {
            throw new deliveryException(deliveryException.errorType.validation_error, "stock cannot be negative");
        }
        this.id = id;
        this.name = name;
        this.category = category;
        this.preperationTime = preperationTime;
        this.stock = stock;
        this.avaliable = true;
    }
    public abstract BigDecimal getPrice(BigDecimal quantity);
    public static class standard extends menuItem {
        private BigDecimal price;
        public standard(String id, String name, String category, int preperationTime, BigDecimal stock, BigDecimal price) {
            super(id, name, category, preperationTime, stock);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new deliveryException(deliveryException.errorType.validation_error, "price must be greater than zero");
            }
            this.price = price;
        }
        @Override
        public BigDecimal getPrice(BigDecimal quantity) {
            return price.multiply(quantity);
        }
    }
    public static class weighted extends menuItem {
        private BigDecimal pricePerKg;
        public weighted(String id, String name, String category, int preperationTime, BigDecimal stock, BigDecimal pricePerKg) {
            super(id, name, category, preperationTime, stock);
            if (pricePerKg == null || pricePerKg.compareTo(BigDecimal.ZERO) <= 0) {
                throw new deliveryException(deliveryException.errorType.validation_error, "price must be greater than zero");
            }
            this.pricePerKg = pricePerKg;
        }
        @Override
        public BigDecimal getPrice(BigDecimal quantity) {
            return pricePerKg.multiply(quantity);
        }
    }
    public static class combo extends menuItem {
        private List<menuItem> items;
        private BigDecimal discount;
        public combo(String id, String name, String category, int preperationTime, BigDecimal stock, List<menuItem> items, BigDecimal discount) {
            super(id, name, category, preperationTime, stock);
            if (items == null || items.isEmpty()) {
                throw new deliveryException(deliveryException.errorType.validation_error, "combo must contain items");
            }
            if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0 || discount.compareTo(BigDecimal.ONE) > 0) {
                throw new deliveryException(deliveryException.errorType.validation_error, "discount must be between zero and one");
            }
            this.items = new ArrayList<>(items);
            this.discount = discount;
        }
        @Override
        public BigDecimal getPrice(BigDecimal quantity) {
            BigDecimal total = BigDecimal.ZERO;
            for (menuItem item : items) {
                total = total.add(item.getPrice(BigDecimal.ONE));
            }
            total = total.multiply(BigDecimal.ONE.subtract(discount));
            return total.multiply(quantity);
        }
        public List<menuItem> getItems() {
            return Collections.unmodifiableList(items);
        }
    }
    public static menuItem create(String type, String id, String name, String category, int preperationTime, BigDecimal stock, BigDecimal price, List<menuItem> items, BigDecimal discount) {
        if (type.equalsIgnoreCase("standard")) {
            return new standard(id, name, category, preperationTime, stock, price);
        }
        if (type.equalsIgnoreCase("weighted")) {
            return new weighted(id, name, category, preperationTime, stock, price);
        }
        if (type.equalsIgnoreCase("combo")) {
            return new combo(id, name, category, preperationTime, stock, items, discount);
        }
        throw new deliveryException(deliveryException.errorType.validation_error, "invalid menu item type");
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getCategory() {
        return category;
    }
    public int getPreperationTime() {
        return preperationTime;
    }
    public boolean isAvailable() {
        return avaliable;
    }
    public void setAvailable(boolean value) {
        avaliable = value;
    }
    public BigDecimal getStock() {
        return stock;
    }
    public void addStock(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new deliveryException(deliveryException.errorType.validation_error, "stock must be greater than zero");
        }
        stock = stock.add(value);
    }
    public void reduceStock(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new deliveryException(deliveryException.errorType.validation_error, "quantity must be greater than zero");
        }
        if (stock.compareTo(value) < 0) {
            throw new deliveryException(deliveryException.errorType.stock_shortage, "not enough stock for " + name);
        }
        stock = stock.subtract(value);
    }
    public void restoreStock(BigDecimal value) {
        stock = stock.add(value);
    }
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof menuItem)) return false;
        menuItem other = (menuItem) obj;
        return Objects.equals(id, other.id) && Objects.equals(restaurantId, other.restaurantId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, restaurantId);
    }
    @Override
    public String toString() {
        return id + " " + name + " " + getPrice(BigDecimal.ONE) + " egp";
    }
}