import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class customer {
    public enum tier {
        bronze,
        silver,
        gold
    }

    public static class address {
        private String district;
        private String detail;

        public address(String district, String detail) {
            if (district == null || district.isBlank()) {
                throw new deliveryException(deliveryException.errorType.validation_error, "district is required");
            }
            if (detail == null || detail.isBlank()) {
                throw new deliveryException(deliveryException.errorType.validation_error, "address detail is required");
            }
            this.district = district;
            this.detail = detail;
        }

        public String getDistrict() {
            return district;
        }

        public String getDetail() {
            return detail;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof address)) return false;
            address other = (address) obj;
            return district.equalsIgnoreCase(other.district) && detail.equalsIgnoreCase(other.detail);
        }

        @Override
        public int hashCode() {
            return Objects.hash(district.toLowerCase(), detail.toLowerCase());
        }

        @Override
        public String toString() {
            return district + " " + detail;
        }
    }

    private String id;
    private String name;
    private String mobile;
    private List<address> adressList;
    private BigDecimal walletBalance;
    private int completedOrders;
    private Deque<String> searches;

    public customer(String id, String name, String mobile, BigDecimal walletBalance) {
        if (id == null || id.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "customer id is required");
        }
        if (name == null || name.isBlank()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "customer name is required");
        }
        if (mobile == null || !mobile.matches("01(0|1|2|5)[0-9]{8}")) {
            throw new deliveryException(deliveryException.errorType.validation_error, "invalid mobile number");
        }
        if (walletBalance == null || walletBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new deliveryException(deliveryException.errorType.validation_error, "wallet balance cannot be negative");
        }
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.walletBalance = walletBalance;
        adressList = new ArrayList<>();
        searches = new ArrayDeque<>();
        completedOrders = 0;
    }

    public void addAddress(address value) {
        if (!adressList.contains(value)) {
            adressList.add(value);
        }
    }

    public boolean ownsAddress(address value) {
        return adressList.contains(value);
    }

    public List<address> getAddresses() {
        return Collections.unmodifiableList(adressList);
    }

    public void addSearch(String value) {
        if (value == null || value.isBlank()) return;
        searches.addFirst(value);
        if (searches.size() > 5) {
            searches.removeLast();
        }
    }

    public List<String> getSearches() {
        return Collections.unmodifiableList(new ArrayList<>(searches));
    }

    public void addMoney(BigDecimal value) {
        walletBalance = walletBalance.add(value);
    }

    public void removeMoney(BigDecimal value) {
        if (walletBalance.compareTo(value) < 0) {
            throw new deliveryException(deliveryException.errorType.insufficient_funds, "wallet balance is not enough");
        }
        walletBalance = walletBalance.subtract(value);
    }

    public void addCompletedOrder() {
        completedOrders++;
    }

    public tier getTier() {
        if (completedOrders >= 30) return tier.gold;
        if (completedOrders >= 10) return tier.silver;
        return tier.bronze;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMobile() {
        return mobile;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public int getCompletedOrders() {
        return completedOrders;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof customer)) return false;
        customer other = (customer) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id + " " + name + " " + getTier();
    }
}