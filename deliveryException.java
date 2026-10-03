public class deliveryException extends RuntimeException {
    public enum errorType {
        restaurant_closed,
        item_unavailable,
        insufficient_funds,
        illegal_status,
        promotion_error,
        rider_busy,
        stock_shortage,
        validation_error
    }
    private errorType type;
    public deliveryException(errorType type, String message) {
        super(message);
        this.type = type;
    }
    public errorType getType() {
        return type;
    }
}