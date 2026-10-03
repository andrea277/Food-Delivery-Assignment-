import java.util.Objects;
public class rider {
    public enum vehicle {
        motorcycle,
        bicycle,
        car
    }
    public interface strategy {
        boolean canTake(order value);
        double getSpeed();
        double getMaxDistance();
        int getMaxItems();
    }
    public static class motorcycleStrategy implements strategy {
        @Override
        public boolean canTake(order value) {
            return value.getDistanceKm().doubleValue() <= 20 && value.getLines().size() <= 5;
        }
        @Override
        public double getSpeed() {
            return 45;
        }
        @Override
        public double getMaxDistance() {
            return 20;
        }
        @Override
        public int getMaxItems() {
            return 5;
        }
    }
    public static class bicycleStrategy implements strategy {
        @Override
        public boolean canTake(order value) {
            return value.getDistanceKm().doubleValue() <= 8 && value.getLines().size() <= 3;
        }
        @Override
        public double getSpeed() {
            return 15;
        }
        @Override
        public double getMaxDistance() {
            return 8;
        }
        @Override
        public int getMaxItems() {
            return 3;
        }
    }
    public static class carStrategy implements strategy {
        @Override
        public boolean canTake(order value) {
            return value.getDistanceKm().doubleValue() <= 40 && value.getLines().size() <= 20;
        }
        @Override
        public double getSpeed() {
            return 35;
        }
        @Override
        public double getMaxDistance() {
            return 40;
        }
        @Override
        public int getMaxItems() {
            return 20;
        }
    }
    private String id;
    private String name;
    private vehicle vehicleType;
    private String district;
    private boolean available;
    private int completedDeleviries;
    private order activeOrder;
    private strategy riderStrategy;
    public rider(String id, String name, vehicle vehicleType, String district) {
        this.id = id;
        this.name = name;
        this.vehicleType = vehicleType;
        this.district = district;
        this.available = false;
        this.completedDeleviries = 0;
        if (vehicleType == vehicle.motorcycle) {
            riderStrategy = new motorcycleStrategy();
        } else if (vehicleType == vehicle.bicycle) {
            riderStrategy = new bicycleStrategy();
        } else {
            riderStrategy = new carStrategy();
        }
    }
    public void goOnDuty() {
        available = true;
    }
    public void goOffDuty() {
        if (activeOrder != null) {
            throw new deliveryException(deliveryException.errorType.rider_busy, "rider has an active order");
        }
        available = false;
    }
    public void assign(order value) {
        if (!available) {
            throw new deliveryException(deliveryException.errorType.rider_busy, "rider is not available");
        }
        if (activeOrder != null) {
            throw new deliveryException(deliveryException.errorType.rider_busy, "rider already has an active order");
        }
        if (!riderStrategy.canTake(value)) {
            throw new deliveryException(deliveryException.errorType.validation_error, "order does not match rider limits");
        }
        activeOrder = value;
        available = false;
        value.setRiderId(id);
        value.changeStatus(order.status.assigned);
    }
    public void pickedUp() {
        if (activeOrder == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "no active order");
        }
        activeOrder.changeStatus(order.status.out_for_delivery);
    }
    public void delivered() {
        if (activeOrder == null) {
            throw new deliveryException(deliveryException.errorType.validation_error, "no active order");
        }
        activeOrder.changeStatus(order.status.delivered);
        completedDeleviries++;
        activeOrder = null;
        available = true;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public vehicle getVehicleType() {
        return vehicleType;
    }
    public String getDistrict() {
        return district;
    }
    public boolean isAvailable() {
        return available;
    }
    public int getCompletedDeliveries() {
        return completedDeleviries;
    }
    public order getActiveOrder() {
        return activeOrder;
    }
    public strategy getStrategy() {
        return riderStrategy;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof rider)) return false;
        rider other = (rider) obj;
        return Objects.equals(id, other.id);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        return id + " " + name + " " + vehicleType;
    }
}