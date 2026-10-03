import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class order {
    public enum status {
        placed,
        accepted,
        preparing,
        ready,
        assigned,
        out_for_delivery,
        delivered,
        cancelled
    }

    public static class lineItem {
        private menuItem item;
        private BigDecimal quantity;

        public lineItem(menuItem item, BigDecimal quantity) {
            if (item == null) {
                throw new deliveryException(deliveryException.errorType.validation_error, "item is required");
            }
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new deliveryException(deliveryException.errorType.validation_error, "quantity must be greater than zero");
            }
            this.item = item;
            this.quantity = quantity;
        }

        public menuItem getItem() {
            return item;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public BigDecimal getPrice() {
            return item.getPrice(quantity);
        }
    }

    public static class price {
        private BigDecimal subtotal;
        private BigDecimal deliveryFee;
        private BigDecimal serviceFee;
        private BigDecimal promotionDiscount;
        private BigDecimal total;

        public price(BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal serviceFee, BigDecimal promotionDiscount, BigDecimal total) {
            this.subtotal = subtotal;
            this.deliveryFee = deliveryFee;
            this.serviceFee = serviceFee;
            this.promotionDiscount = promotionDiscount;
            this.total = total;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public BigDecimal getDeliveryFee() {
            return deliveryFee;
        }

        public BigDecimal getServiceFee() {
            return serviceFee;
        }

        public BigDecimal getPromotionDiscount() {
            return promotionDiscount;
        }

        public BigDecimal getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return "subtotal: " + subtotal + "\n" + "delivery fee: " + deliveryFee + "\n" + "service fee: " + serviceFee + "\n" + "promotion discount: " + promotionDiscount + "\n" + "total: " + total;
        }
    }

    public interface observer {
        void update(order value, status oldStatus, status newStatus);
    }

    public static class builder {
        private String id;
        private customer orderCustomer;
        private restaurant restaurant;
        private customer.address address;
        private BigDecimal distanceKm;
        private String deliveryNotes;
        private promotion promotion;
        private List<lineItem> lines = new ArrayList<>();

        public builder setId(String id) {
            this.id = id;
            return this;
        }

        public builder setCustomer(customer customer) {
            this.orderCustomer = customer;
            return this;
        }

        public builder setRestaurant(restaurant restaurant) {
            this.restaurant = restaurant;
            return this;
        }

        public builder setAddress(customer.address address) {
            this.address = address;
            return this;
        }

        public builder setDistanceKm(BigDecimal distanceKm) {
            this.distanceKm = distanceKm;
            return this;
        }

        public builder setDeliveryNotes(String deliveryNotes) {
            this.deliveryNotes = deliveryNotes;
            return this;
        }

        public builder addItem(menuItem item, BigDecimal quantity) {
            lines.add(new lineItem(item, quantity));
            return this;
        }

        public builder setPromotion(promotion promotion) {
            this.promotion = promotion;
            return this;
        }

        public order build() {
            if (id == null || id.isBlank()) {
                throw new deliveryException(deliveryException.errorType.validation_error, "order id is required");
            }
            if (orderCustomer == null) {
                throw new deliveryException(deliveryException.errorType.validation_error, "customer is required");
            }
            if (restaurant == null) {
                throw new deliveryException(deliveryException.errorType.validation_error, "restaurant is required");
            }
            if (address == null) {
                throw new deliveryException(deliveryException.errorType.validation_error, "address is required");
            }
            if (distanceKm == null || distanceKm.compareTo(BigDecimal.ZERO) < 0) {
                throw new deliveryException(deliveryException.errorType.validation_error, "distance cannot be negative");
            }
            if (lines.isEmpty()) {
                throw new deliveryException(deliveryException.errorType.validation_error, "order must contain at least one line item");
            }
            return new order(this);
        }
    }

    private String id;
    private customer orderCustomer;
    private restaurant restaurant;
    private customer.address address;
    private BigDecimal distanceKm;
    private String deliveryNotes;
    private List<lineItem> lines;
    private promotion promotion;
    private status currentStatus;
    private LocalDateTime placedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private boolean paid;
    private BigDecimal paidAmount;
    private price finalPrice;
    private String riderId;
    private List<observer> observers;

    private order(builder value) {
        this.id = value.id;
        this.orderCustomer = value.orderCustomer;
        this.restaurant = value.restaurant;
        this.address = value.address;
        this.distanceKm = value.distanceKm;
        this.deliveryNotes = value.deliveryNotes;
        this.lines = new ArrayList<>(value.lines);
        this.promotion = value.promotion;
        this.currentStatus = status.placed;
        this.placedAt = LocalDateTime.now();
        this.paid = false;
        this.paidAmount = BigDecimal.ZERO;
        this.observers = new ArrayList<>();
    }

    public void addObserver(observer value) {
        observers.add(value);
    }

    public void setFinalPrice(price value) {
        finalPrice = value;
    }

    public void setRiderId(String riderId) {
        this.riderId = riderId;
    }

    public void changeStatus(status newStatus) {
        if (!legal(currentStatus, newStatus)) {
            throw new deliveryException(deliveryException.errorType.illegal_status, "illegal order transition");
        }
        status oldStatus = currentStatus;
        currentStatus = newStatus;
        if (newStatus == status.out_for_delivery) {
            pickedUpAt = LocalDateTime.now();
        }
        if (newStatus == status.delivered) {
            deliveredAt = LocalDateTime.now();
            orderCustomer.addCompletedOrder();
            restaurant.addCompletedOrder();
        }
        for (observer value : observers) {
            value.update(this, oldStatus, newStatus);
        }
    }

    private boolean legal(status oldStatus, status newStatus) {
        if (oldStatus == status.delivered || oldStatus == status.cancelled) {
            return false;
        }
        if (newStatus == status.cancelled) {
            return oldStatus == status.placed
                    || oldStatus == status.accepted
                    || oldStatus == status.preparing
                    || oldStatus == status.ready
                    || oldStatus == status.assigned;
        }
        if (oldStatus == status.placed && newStatus == status.accepted) {
            return true;
        }
        if (oldStatus == status.accepted && newStatus == status.preparing) {
            return true;
        }
        if (oldStatus == status.preparing && newStatus == status.ready) {
            return true;
        }
        if (oldStatus == status.ready && newStatus == status.assigned) {
            return true;
        }
        if (oldStatus == status.assigned && newStatus == status.out_for_delivery) {
            return true;
        }
        return oldStatus == status.out_for_delivery && newStatus == status.delivered;
    }

    public price calculatePrice() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (lineItem line : lines) {
            subtotal = subtotal.add(line.getPrice());
        }
        BigDecimal deliveryFee = new BigDecimal("15");
        BigDecimal freeDistance = new BigDecimal("3");
        BigDecimal extraKmFee = new BigDecimal("3");
        if (distanceKm.compareTo(freeDistance) > 0) {
            BigDecimal extraKm = distanceKm.subtract(freeDistance);
            deliveryFee = deliveryFee.add(extraKm.multiply(extraKmFee));
        }

        // No longer throws an error because orderCustomer is the variable, and customer is the class
        if (orderCustomer.getTier() == customer.tier.silver) {
            deliveryFee = deliveryFee.multiply(new BigDecimal("0.90"));
        }
        if (orderCustomer.getTier() == customer.tier.gold) {
            deliveryFee = BigDecimal.ZERO;
        }

        deliveryFee = deliveryFee.setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = subtotal.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal promotionDiscount = BigDecimal.ZERO;
        BigDecimal deliveryDiscount = BigDecimal.ZERO;

        if (promotion != null) {
            String promotionError = promotion.check(this, subtotal);
            if (promotionError != null) {
                throw new deliveryException(deliveryException.errorType.promotion_error, promotionError);
            }
            promotionDiscount = promotion.getSubtotalDiscount(this, subtotal);
            deliveryDiscount = promotion.getDeliveryDiscount(this, deliveryFee);
        }

        deliveryFee = deliveryFee.subtract(deliveryDiscount);
        if (deliveryFee.compareTo(BigDecimal.ZERO) < 0) {
            deliveryFee = BigDecimal.ZERO;
        }

        BigDecimal total = subtotal.add(deliveryFee).add(serviceFee).subtract(promotionDiscount);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        return new price(subtotal, deliveryFee, serviceFee, promotionDiscount, total.setScale(2, RoundingMode.HALF_UP));
    }

    public void pay() {
        if (paid) {
            throw new deliveryException(deliveryException.errorType.validation_error, "order is already paid");
        }
        BigDecimal total = getTotal();
        orderCustomer.removeMoney(total);
        paidAmount = total;
        paid = true;
    }

    public void cancel() {
        if (!legal(currentStatus, status.cancelled)) {
            throw new deliveryException(deliveryException.errorType.illegal_status, "order cannot be cancelled now");
        }
        if (paid) {
            orderCustomer.addMoney(paidAmount);
        }
        for (lineItem line : lines) {
            line.getItem().restoreStock(line.getQuantity());
        }
        changeStatus(status.cancelled);
    }

    public long getElapsedMinutes() {
        return Duration.between(placedAt, LocalDateTime.now()).toMinutes();
    }

    public long getDeliveryMinutes() {
        if (pickedUpAt == null || deliveredAt == null) {
            return 0;
        }
        return Duration.between(pickedUpAt, deliveredAt).toMinutes();
    }

    public String getId() {
        return id;
    }

    public customer getCustomer() {
        return orderCustomer;
    }

    public restaurant getRestaurant() {
        return restaurant;
    }

    public customer.address getAddress() {
        return address;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public String getDeliveryNotes() {
        return deliveryNotes;
    }

    public List<lineItem> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public promotion getPromotion() {
        return promotion;
    }

    public status getStatus() {
        return currentStatus;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public boolean isPaid() {
        return paid;
    }

    public String getRiderId() {
        return riderId;
    }

    public BigDecimal getTotal() {
        if (finalPrice != null) {
            return finalPrice.getTotal();
        }
        return calculatePrice().getTotal();
    }

    public price getFinalPrice() {
        if (finalPrice != null) {
            return finalPrice;
        }
        return calculatePrice();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof order)) return false;
        order other = (order) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " " + currentStatus + " " + getTotal();
    }
}