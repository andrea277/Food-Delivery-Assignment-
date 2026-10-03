import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static platform appPlatform = platform.getInstance();
    private static reports reportSystem = new reports(appPlatform);
    public static void main(String[] args) {
        addSampleData();
        while (true) {
            System.out.println();
            System.out.println("============================masr delivery==================================================");
            System.out.println("1.customer");
            System.out.println("2.restaurant");
            System.out.println("3.rider");
            System.out.println("4.admin&reports");
            System.out.println("0.exit");
            System.out.print("choose:");
            int menuChoice = readInt();
            try {
                if (menuChoice == 1) {
                    customerArea();
                } else if (menuChoice == 2) {
                    restaurantArea();
                } else if (menuChoice == 3) {
                    riderArea();
                } else if (menuChoice == 4) {
                    adminArea();
                } else if (menuChoice == 0) {
                    return;
                } else {
                    System.out.println("invalid option");
                }
            } catch (deliveryException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private static void addSampleData() {
        restaurant restaurantOne = new restaurant("r1", "piza house", "maadi", List.of("pizza", "italian"), 4.7);
        restaurantOne.addMenuItem(new menuItem.standard("i1", "margrita", "pizza", 20, new BigDecimal("100"), new BigDecimal("120")));
        restaurantOne.addMenuItem(new menuItem.standard("i2", "cola", "drink", 5, new BigDecimal("100"), new BigDecimal("30")));
        restaurantOne.addMenuItem(new menuItem.weighted("i3", "grilled meat", "meat", 30, new BigDecimal("100"), new BigDecimal("300")));
        List<menuItem> comboParts = new ArrayList<>();
        comboParts.add(restaurantOne.getMenuItem("i1"));
        comboParts.add(restaurantOne.getMenuItem("i2"));
        restaurantOne.addMenuItem(new menuItem.combo("i4", "meal combo", "combo", 25, new BigDecimal("50"), comboParts, new BigDecimal("0.10")));
        appPlatform.addRestaurant(restaurantOne);
        customer customerOne = new customer("c1", "ahmed", "01012345678", new BigDecimal("2000"));
        customerOne.addAddress(new customer.address("faisal", "floor 5"));
        customerOne.addAddress(new customer.address("dokki", "floor 6"));
        appPlatform.addCustomer(customerOne);
        rider riderOne = new rider("d1", "mohamed", rider.vehicle.motorcycle, "maadi");
        appPlatform.addRider(riderOne);
        appPlatform.addPromotion(new promotion.percentage("nile20", LocalDate.of(2099, 12, 31), BigDecimal.ZERO, null, false, new BigDecimal("0.20"), new BigDecimal("50")));
    }
    private static void customerArea() {
        System.out.print("customer id:");
        customer customerOne = appPlatform.getCustomer(scanner.nextLine());
        while (true) {
            System.out.println();
            System.out.println("1.browse restauran");
            System.out.println("2.search restauran");
            System.out.println("3.filter restauran");
            System.out.println("4.view menu");
            System.out.println("5.place order");
            System.out.println("6.pay");
            System.out.println("7.track order");
            System.out.println("8.cancel order");
            System.out.println("9.order history");
            System.out.println("10.search history");
            System.out.println("0.back");
            System.out.print("choose:");
            int menuChoice = readInt();
            if (menuChoice == 0) {
                return;
            }
            if (menuChoice == 1) {
                List<restaurant> restaurantList = appPlatform.browse();
                if (restaurantList.isEmpty()) {
                    System.out.println("nothingfound");
                }
                for (restaurant restaurant : restaurantList) {
                    System.out.println(restaurant);
                }
            } else if (menuChoice == 2) {
                System.out.print("search");
                String searchText = scanner.nextLine();
                List<restaurant> restaurantList = appPlatform.search(customerOne, searchText);
                if (restaurantList.isEmpty()) {
                    System.out.println("nothing found");
                }
                for (restaurant restaurant : restaurantList) {
                    System.out.println(restaurant);
                }
            } else if (menuChoice == 3) {
                System.out.print("district or empty:");
                String district = scanner.nextLine();
                System.out.print("cuisne or empty:");
                String cuisine = scanner.nextLine();
                System.out.print("minimum rating or empty:");
                String ratingText = scanner.nextLine();
                Double minRating = null;
                if (!ratingText.isBlank()) {
                    minRating = Double.parseDouble(ratingText);
                }
                System.out.print("maximum price or empty:");
                String priceText = scanner.nextLine();
                BigDecimal maxPrice = null;
                if (!priceText.isBlank()) {
                    maxPrice = new BigDecimal(priceText);
                }
                List<restaurant> restaurantList = appPlatform.filter(customerOne, district, cuisine, minRating, maxPrice);
                if (restaurantList.isEmpty()) {
                    System.out.println("nothing found");
                }
                for (restaurant restaurant : restaurantList) {
                    System.out.println(restaurant);
                }
            } else if (menuChoice == 4) {
                viewMenu();
            } else if (menuChoice == 5) {
                placeOrder(customerOne);
            } else if (menuChoice == 6) {
                System.out.print("order id:");
                order orderOne = appPlatform.getOrder(scanner.nextLine());
                orderOne.pay();
                System.out.println("payment successful");
                System.out.println("new balance:" + customerOne.getWalletBalance());
            } else if (menuChoice == 7) {
                System.out.print("order id:");
                order orderOne = appPlatform.getOrder(scanner.nextLine());
                System.out.println("status:" + orderOne.getStatus());
                System.out.println("elapsed:" + orderOne.getElapsedMinutes() + "minutes");
            } else if (menuChoice == 8) {
                System.out.print("order id:");
                order orderOne = appPlatform.getOrder(scanner.nextLine());
                orderOne.cancel();
                System.out.println("order cancelled");
            } else if (menuChoice == 9) {
                List<order> orderList = reportSystem.customerHistory(customerOne.getId());
                if (orderList.isEmpty()) {
                    System.out.println("no orders found");
                }
                for (order value : orderList) {
                    System.out.println(value);
                }
                System.out.println("total spent:" + reportSystem.customerSpent(customerOne.getId()));
            } else if (menuChoice == 10) {
                List<String> searchList = customerOne.getSearches();
                if (searchList.isEmpty()) {
                    System.out.println("no searches");
                }
                for (String value : searchList) {
                    System.out.println(value);
                }
            } else {
                System.out.println("invalid option");
            }
        }
    }
    private static void viewMenu() {
        System.out.print("restaurant id:");
        restaurant restaurantOne = appPlatform.getRestaurant(scanner.nextLine());
        Map<String, List<menuItem>> groupedMenu = restaurantOne.getMenu().stream().collect(Collectors.groupingBy(menuItem::getCategory, LinkedHashMap::new, Collectors.toList()));
        for (String category : groupedMenu.keySet()) {
            System.out.println(category);
            for (menuItem item : groupedMenu.get(category)) {
                System.out.println(item.getId() + " " + item.getName() + " " + item.getPrice(BigDecimal.ONE) + " " + (item.isAvailable() ? "available" : "unavailable"));
            }
        }
    }
    private static void placeOrder(customer customerOne) {
        System.out.print("restaurant id:");
        restaurant restaurantOne = appPlatform.getRestaurant(scanner.nextLine());
        List<customer.address> addressList = customerOne.getAddresses();
        for (int i = 0; i < addressList.size(); i++) {
            System.out.println((i + 1) + "  " + addressList.get(i));
        }
        System.out.print("address number: ");
        int addressNumber = readInt();
        if (addressNumber < 1 || addressNumber > addressList.size()) {
            throw new deliveryException(deliveryException.errorType.validation_error, "invalid address number");
        }
        System.out.print("order id:");
        String orderId = scanner.nextLine();
        System.out.print("distance km:");
        BigDecimal distanceKm = readDecimal();
        System.out.print("delivery notes or empty:");
        String deliveryNotes = scanner.nextLine();
        order.builder orderBuilder = new order.builder().setId(orderId).setCustomer(customerOne).setRestaurant(restaurantOne).setAddress(addressList.get(addressNumber - 1)).setDistanceKm(distanceKm).setDeliveryNotes(deliveryNotes);
        while (true) {
            System.out.print("item id or 0:");
            String itemId = scanner.nextLine();
            if (itemId.equals("0")) {
                break;
            }
            menuItem item = restaurantOne.getMenuItem(itemId);
            System.out.print("quantity:");
            BigDecimal quantity = readDecimal();
            orderBuilder.addItem(item, quantity);
        }
        System.out.print("promotion code or 0:");
        String promotionCode = scanner.nextLine();
        if (!promotionCode.equals("0")) {
            orderBuilder.setPromotion(appPlatform.getPromotion(promotionCode));
        }
        order orderOne = orderBuilder.build();
        appPlatform.placeOrder(orderOne);
        System.out.println(orderOne.getFinalPrice());
    }
    private static void restaurantArea() {
        System.out.print("restaurant id:");
        restaurant restaurantOne = appPlatform.getRestaurant(scanner.nextLine());
        while (true) {
            System.out.println();
            System.out.println("1.accept order");
            System.out.println("2.reject order");
            System.out.println("3.preparing");
            System.out.println("4.ready");
            System.out.println("5.toggle item");
            System.out.println("6.add item");
            System.out.println("7.remove item");
            System.out.println("8.adjust stock");
            System.out.println("9.today orders");
            System.out.println("10.today revenue");
            System.out.println("11.view menu");
            System.out.println("0.back");
            System.out.print("choose:");
            int choice = readInt();
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                System.out.print("order id:");
                appPlatform.acceptOrder(scanner.nextLine());
            } else if (choice == 2) {
                System.out.print("order id:");
                appPlatform.rejectOrder(scanner.nextLine());
            } else if (choice == 3) {
                System.out.print("order id:");
                appPlatform.markPreparing(scanner.nextLine());
            } else if (choice == 4) {
                System.out.print("order id:");
                appPlatform.markReady(scanner.nextLine());
            } else if (choice == 5) {
                System.out.print("item id:");
                menuItem item = restaurantOne.getMenuItem(scanner.nextLine());
                item.setAvailable(!item.isAvailable());
            } else if (choice == 6) {
                addMenuItem(restaurantOne);
            } else if (choice == 7) {
                System.out.print("item id:");
                restaurantOne.removeMenuItem(scanner.nextLine());
            } else if (choice == 8) {
                System.out.print("item id:");
                menuItem item = restaurantOne.getMenuItem(scanner.nextLine());
                System.out.print("stock to add:");
                item.addStock(readDecimal());
            } else if (choice == 9) {
                List<order> orderList = reportSystem.todayRestaurantOrders(restaurantOne);
                for (order value : orderList) {
                    System.out.println(value);
                }
            } else if (choice == 10) {
                System.out.println(reportSystem.todayRestaurantRevenue(restaurantOne));
            } else if (choice == 11) {
                for (menuItem item : restaurantOne.getMenu()) {
                    System.out.println(item);
                }
            } else {
                System.out.println("invalid option");
            }
        }
    }
    private static void addMenuItem(restaurant restaurantOne) {
        String type;
        while (true) {
            System.out.print("type standard weighted combo: ");
            type = scanner.nextLine().toLowerCase();
            if (type.equals("standard") || type.equals("weighted") || type.equals("combo")) {
                break;
            }
            System.out.println("invalid type try again");
        }
        System.out.print("id:");
        String id = scanner.nextLine();
        System.out.print("name:");
        String name = scanner.nextLine();
        System.out.print("category:");
        String category = scanner.nextLine();
        System.out.print("preparation time:");
        int preperationTime = readInt();
        System.out.print("stock:");
        BigDecimal stock = readDecimal();
        System.out.print("price:");
        BigDecimal price = readDecimal();
        List<menuItem> parts = new ArrayList<>();
        BigDecimal discount = BigDecimal.ZERO;
        if (type.equals("combo")) {
            while (true) {
                System.out.print("part id or 0:");
                String partId = scanner.nextLine();
                if (partId.equals("0")) {
                    break;
                }
                parts.add(restaurantOne.getMenuItem(partId));
            }
            System.out.print("discount as decimal:");
            discount = readDecimal();
        }
        menuItem item = menuItem.create(type, id, name, category, preperationTime, stock, price, parts, discount);
        restaurantOne.addMenuItem(item);
        System.out.println("item added");
    }
    private static void riderArea() {
        System.out.print("rider id: ");
        rider riderOne = appPlatform.getRider(scanner.nextLine());
        while (true) {
            System.out.println();
            System.out.println("1. go on duty");
            System.out.println("2. go off duty");
            System.out.println("3. current order");
            System.out.println("4. get next order");
            System.out.println("5. mark picked up");
            System.out.println("6. mark delivered");
            System.out.println("7. statistics");
            System.out.println("0. back");
            System.out.print("choose: ");
            int choice = readInt();
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                riderOne.goOnDuty();
                System.out.println("rider is on duty");
            } else if (choice == 2) {
                riderOne.goOffDuty();
                System.out.println("rider is off duty");
            } else if (choice == 3) {
                if (riderOne.getActiveOrder() == null) {
                    System.out.println("no current order");
                } else {
                    System.out.println(riderOne.getActiveOrder().getId());
                }
            } else if (choice == 4) {
                appPlatform.assignNext(riderOne.getId());
                System.out.println("order assigned");
            } else if (choice == 5) {
                riderOne.pickedUp();
                System.out.println("order picked up");
            } else if (choice == 6) {
                riderOne.delivered();
                System.out.println("order delivered");
            } else if (choice == 7) {
                System.out.println("completed deliveries:" + riderOne.getCompletedDeliveries());
            } else {
                System.out.println("invalid option");
            }
        }
    }
    private static void adminArea() {
        while (true) {
            System.out.println();
            System.out.println("1.add restaurant");
            System.out.println("2.remove restaurant");
            System.out.println("3.create promotion");
            System.out.println("4.reports");
            System.out.println("5.platform statistics");
            System.out.println("0.back");
            System.out.print("choose:");
            int choice = readInt();
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                addRestaurant();
            } else if (choice == 2) {
                System.out.print("restaurant id:");
                appPlatform.removeRestaurant(scanner.nextLine());
            } else if (choice == 3) {
                createPromotion();
            } else if (choice == 4) {
                reportArea();
            } else if (choice == 5) {
                System.out.println("restaurants:" + appPlatform.getRestaurants().size());
                System.out.println("customers:" + appPlatform.getCustomers().size());
                System.out.println("riders:" + appPlatform.getRiders().size());
                System.out.println("orders:" + appPlatform.getOrders().size());
                System.out.println("cuisines:" + appPlatform.getCuisines());
            } else {
                System.out.println("invalid option");
            }
        }
    }
    private static void addRestaurant() {
        System.out.print("id:");
        String id = scanner.nextLine();
        System.out.print("name:");
        String name = scanner.nextLine();
        System.out.print("district:");
        String district = scanner.nextLine();
        System.out.print("rating:");
        double rating = Double.parseDouble(scanner.nextLine());
        System.out.print("cuisines separated by comma:");
        String cuisineText = scanner.nextLine();
        List<String> cuisineList = new ArrayList<>();
        String[] cuisineArray = cuisineText.split("-");
        for (String value : cuisineArray) {
            value = value.trim().toLowerCase();
            if (!value.isBlank() && !cuisineList.contains(value)) {
                cuisineList.add(value);
            }
        }
        appPlatform.addRestaurant(new restaurant(id, name, district, cuisineList, rating));
        System.out.println("restaurant added");
    }
    private static void createPromotion() {
        String type;
        while (true) {
            System.out.print("type percentage fixed free delivery: ");
            type = scanner.nextLine().toLowerCase();
            if (type.equals("percentage") || type.equals("fixed") || type.equals("free delivery")) {
                break;
            }
            System.out.println("invalid type try again");
        }
        System.out.print("code:");
        String promotionCode = scanner.nextLine();
        System.out.print("expiry yyyy mm dd:");
        LocalDate expiry = LocalDate.parse(scanner.nextLine());
        System.out.print("minimum subtotal:");
        BigDecimal minimumSubtotal = readDecimal();
        System.out.print("district or empty:");
        String district = scanner.nextLine();
        if (district.isBlank()) {
            district = null;
        }
        System.out.print("first customer true false:");
        boolean firstCustomerOnly = Boolean.parseBoolean(scanner.nextLine());
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal maxDiscount = null;
        if (type.equals("percentage")) {
            System.out.print("percentage as decimal");
            amount = readDecimal();
            System.out.print("maximum discount:");
            maxDiscount = readDecimal();
        } else if (type.equals("fixed")) {
            System.out.print("amount");
            amount = readDecimal();
        }
        appPlatform.addPromotion(promotion.create(type, promotionCode, expiry, minimumSubtotal, district, firstCustomerOnly, amount, maxDiscount));
        System.out.println("promotion created");
    }
    private static void reportArea() {
        while (true) {
            System.out.println();
            System.out.println("1. total revenue");
            System.out.println("2. top five restaurants");
            System.out.println("3. average order value by district");
            System.out.println("4. high rated restaurants");
            System.out.println("5. orders by status");
            System.out.println("6. rider statistics");
            System.out.println("7. most ordered item");
            System.out.println("8. customer history");
            System.out.println("9. peak ordering hour");
            System.out.println("10. inactive customers");
            System.out.println("0. back");
            System.out.print("choose:");
            int choice = readInt();
            if (choice == 0) {
                return;
            }
            if (choice == 1) {
                System.out.print("start");
                LocalDate start = LocalDate.parse(scanner.nextLine());
                System.out.print("end");
                LocalDate end = LocalDate.parse(scanner.nextLine());
                System.out.println(reportSystem.totalRevenue(start, end));
            } else if (choice == 2) {
                System.out.print("year");
                int year = readInt();
                System.out.print("month");
                int month = readInt();
                List<Map.Entry<String, BigDecimal>> result = reportSystem.topFiveRestaurants(year, month);
                for (Map.Entry<String, BigDecimal> entry : result) {
                    System.out.println(entry);
                }
            } else if (choice == 3) {
                System.out.println(reportSystem.averageOrderValueByDistrict());
            } else if (choice == 4) {
                for (restaurant restaurant : reportSystem.highRatedRestaurants()) {
                    System.out.println(restaurant);
                }
            } else if (choice == 5) {
                System.out.println(reportSystem.orderCountByStatus());
            } else if (choice == 6) {
                for (String value : reportSystem.riderStats()) {
                    System.out.println(value);
                }
            } else if (choice == 7) {
                System.out.println(reportSystem.mostOrderedItem().orElse("no orders exist"));
            } else if (choice == 8) {
                System.out.print("customer id:");
                String customerId = scanner.nextLine();
                for (order value : reportSystem.customerHistory(customerId)) {
                    System.out.println(value);
                }
                System.out.println("total spent" + reportSystem.customerSpent(customerId));
            } else if (choice == 9) {
                System.out.println(reportSystem.peakOrderingHour().map(x -> "hour:" + x).orElse("no orders exist"));
            } else if (choice == 10) {
                for (customer value : reportSystem.inactiveCustomers()) {
                    System.out.println(value);
                }
            } else {
                System.out.println("invalid option");
            }
        }
    }
    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (Exception e) {
                System.out.print("enter a valid number");
            }
        }
    }
    private static BigDecimal readDecimal() {
        while (true) {
            try {
                BigDecimal value = new BigDecimal(scanner.nextLine());
                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.print("enter a number greater than zero:");
                    continue;
                }
                return value;
            } catch (Exception e) {
                System.out.print("enter a valid amount:");
            }
        }
    }
}