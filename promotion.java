import java.math.BigDecimal;
import java.time.LocalDate;
public abstract class promotion {
    protected String code;
    protected LocalDate expiryDate;
    protected BigDecimal minimumSubtotal;
    protected String district;
    protected boolean firstCustomerOnly;
    public promotion(String code, LocalDate expiryDate, BigDecimal minimumSubtotal, String district, boolean firstCustomerOnly) {
        this.code = code.toLowerCase();
        this.expiryDate = expiryDate;
        this.minimumSubtotal = minimumSubtotal;
        this.district = district;
        this.firstCustomerOnly = firstCustomerOnly;
    }
    public String check(order value, BigDecimal subtotal) {
        if (LocalDate.now().isAfter(expiryDate)) {
            return "promotion has expired";
        }
        if (subtotal.compareTo(minimumSubtotal) < 0) {
            return "subtotal is below promotion minimum";
        }
        if (district != null && !district.equalsIgnoreCase(value.getAddress().getDistrict())) {
            return "promotion is not available in this district";
        }
        if (firstCustomerOnly && value.getCustomer().getCompletedOrders() > 0) {
            return "promotion is for first time customers only";
        }
        return null;
    }
    public abstract BigDecimal getSubtotalDiscount(order value, BigDecimal subtotal);
    public abstract BigDecimal getDeliveryDiscount(order value, BigDecimal deliveryFee);
    public String getCode() {
        return code;
    }
    public static class percentage extends promotion {
        private BigDecimal percentage;
        private BigDecimal maxDiscount;
        public percentage(String code, LocalDate expiryDate, BigDecimal minimumSubtotal, String district, boolean firstCustomerOnly, BigDecimal percentage, BigDecimal maxDiscount) {
            super(code, expiryDate, minimumSubtotal, district, firstCustomerOnly);
            this.percentage = percentage;
            this.maxDiscount = maxDiscount;
        }
        @Override
        public BigDecimal getSubtotalDiscount(order value, BigDecimal subtotal) {
            BigDecimal discount = subtotal.multiply(percentage);
            if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
                discount = maxDiscount;
            }
            return discount;
        }
        @Override
        public BigDecimal getDeliveryDiscount(order value, BigDecimal deliveryFee) {
            return BigDecimal.ZERO;
        }
    }
    public static class fixed extends promotion {
        private BigDecimal amount;
        public fixed(String code, LocalDate expiryDate, BigDecimal minimumSubtotal, String district, boolean firstCustomerOnly, BigDecimal amount) {
            super(code, expiryDate, minimumSubtotal, district, firstCustomerOnly);
            this.amount = amount;
        }
        @Override
        public BigDecimal getSubtotalDiscount(order value, BigDecimal subtotal) {
            if (amount.compareTo(subtotal) > 0) {
                return subtotal;
            }
            return amount;
        }
        @Override
        public BigDecimal getDeliveryDiscount(order value, BigDecimal deliveryFee) {
            return BigDecimal.ZERO;
        }
    }
    public static class freeDelivery extends promotion {
        public freeDelivery(String code, LocalDate expiryDate, BigDecimal minimumSubtotal, String district, boolean firstCustomerOnly) {
            super(code, expiryDate, minimumSubtotal, district, firstCustomerOnly);
        }
        @Override
        public BigDecimal getSubtotalDiscount(order value, BigDecimal subtotal) {
            return BigDecimal.ZERO;
        }
        @Override
        public BigDecimal getDeliveryDiscount(order value, BigDecimal deliveryFee) {
            return deliveryFee;
        }
    }
    public static promotion create(String type, String code, LocalDate expiryDate, BigDecimal minimumSubtotal, String district, boolean firstCustomerOnly, BigDecimal amount, BigDecimal maxDiscount) {
        if (type.equalsIgnoreCase("percentage")) {
            return new percentage(code, expiryDate, minimumSubtotal, district, firstCustomerOnly, amount, maxDiscount);
        }
        if (type.equalsIgnoreCase("fixed")) {
            return new fixed(code, expiryDate, minimumSubtotal, district, firstCustomerOnly, amount);
        }
        if (type.equalsIgnoreCase("free delivery")) {
            return new freeDelivery(code, expiryDate, minimumSubtotal, district, firstCustomerOnly);
        }
        throw new deliveryException(deliveryException.errorType.promotion_error, "invalid promotion type");
    }
}