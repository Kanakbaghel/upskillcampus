import java.util.*;
import java.text.SimpleDateFormat;

public class BankingInformationSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Platform platform = new Platform();

        // Sample data
        platform.registerMerchant(new Merchant("HomeFix", "home", "homefix@gmail.com", "home123"));
        platform.registerMerchant(new Merchant("Beauty Bliss", "beauty", "beautybliss@gmail.com", "beauty123"));
        platform.registerCustomer(new Customer("Kanak", "kanak@gmail.com", "pass123"));

        System.out.println("🏪 Welcome to the Multi-Client Service Platform!");

        while (true) {
            System.out.println("\n1. Merchant Login\n2. Customer Login\n3. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                System.out.print("Enter merchant email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String pwd = scanner.nextLine();
                Merchant m = platform.loginMerchant(email, pwd);
                if (m != null) merchantMenu(platform, m, scanner);
            } else if (choice == 2) {
                System.out.print("Enter customer email: ");
                String email = scanner.nextLine();
                System.out.print("Enter password: ");
                String pwd = scanner.nextLine();
                Customer c = platform.loginCustomer(email, pwd);
                if (c != null) customerMenu(platform, c, scanner);
            } else {
                break;
            }
        }
        scanner.close();
    }

    static void merchantMenu(Platform p, Merchant m, Scanner sc) {
        while (true) {
            System.out.println("\n👨‍🔧 Merchant Dashboard - " + m.name);
            System.out.println("1. Add service\n2. View orders\n3. Logout");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 1) {
                System.out.print("Service name: ");
                String sname = sc.nextLine();
                System.out.print("Category: ");
                String cat = sc.nextLine();
                System.out.print("Price: ");
                double price = sc.nextDouble(); sc.nextLine();
                m.addService(sname, cat, price);
            } else if (ch == 2) {
                p.viewMerchantDashboard(m);
            } else break;
        }
    }

    static void customerMenu(Platform p, Customer c, Scanner sc) {
        while (true) {
            System.out.println("\n🧑‍💼 Customer Dashboard - " + c.name);
            System.out.println("1. Browse services by category\n2. View my orders\n3. Logout");
            int ch = sc.nextInt(); sc.nextLine();
            if (ch == 1) {
                System.out.print("Enter category to browse: ");
                String cat = sc.nextLine();
                p.browseServices(cat);
                System.out.print("Enter Merchant Name: ");
                String mname = sc.nextLine();
                Merchant m = p.findMerchantByName(mname);
                if (m != null) {
                    System.out.print("Service to purchase: ");
                    String sname = sc.nextLine();
                    System.out.print("Schedule (dd-MM-yyyy HH:mm): ");
                    String t = sc.nextLine();
                    try {
                        Date d = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(t);
                        p.placeOrder(c, m, sname, d);
                    } catch (Exception e) {
                        System.out.println("Invalid date format.");
                    }
                }
            } else if (ch == 2) {
                p.viewCustomerOrders(c);
            } else break;
        }
    }
}

// ------------------------- Entities and Platform -------------------------

abstract class User {
    String name, email, password;

    User(String name, String email, String pwd) {
        this.name = name;
        this.email = email;
        this.password = pwd;
    }

    boolean authenticate(String email, String pwd) {
        return this.email.equals(email) && this.password.equals(pwd);
    }
}

class Merchant extends User {
    String category;
    List<Service> services = new ArrayList<>();
    List<Order> orders = new ArrayList<>();

    Merchant(String name, String cat, String email, String pwd) {
        super(name, email, pwd);
        this.category = cat;
    }

    void addService(String name, String category, double price) {
        services.add(new Service(name, category, price));
        System.out.println("✅ Service added: " + name);
    }

    Optional<Service> getService(String name) {
        return services.stream().filter(s -> s.name.equalsIgnoreCase(name)).findFirst();
    }

    void receiveOrder(Order o) {
        orders.add(o);
    }
}

class Customer extends User {
    List<Order> orderHistory = new ArrayList<>();

    Customer(String name, String email, String pwd) {
        super(name, email, pwd);
    }

    void addOrder(Order o) {
        orderHistory.add(o);
    }
}

class Service {
    String name, category;
    double price;

    Service(String name, String cat, double price) {
        this.name = name;
        this.category = cat;
        this.price = price;
    }
}

class Order {
    Customer customer;
    Merchant merchant;
    Service service;
    Date appointment;
    Date orderDate = new Date();

    Order(Customer c, Merchant m, Service s, Date app) {
        this.customer = c;
        this.merchant = m;
        this.service = s;
        this.appointment = app;
    }
}

class Platform {
    List<Merchant> merchants = new ArrayList<>();
    List<Customer> customers = new ArrayList<>();

    void registerMerchant(Merchant m) {
        merchants.add(m);
    }

    void registerCustomer(Customer c) {
        customers.add(c);
    }

    Merchant loginMerchant(String email, String pwd) {
        for (Merchant m : merchants)
            if (m.authenticate(email, pwd)) return m;
        System.out.println("❌ Merchant login failed.");
        return null;
    }

    Customer loginCustomer(String email, String pwd) {
        for (Customer c : customers)
            if (c.authenticate(email, pwd)) return c;
        System.out.println("❌ Customer login failed.");
        return null;
    }

    void browseServices(String category) {
        System.out.println("\n📋 Services in '" + category + "' category:");
        for (Merchant m : merchants) {
            for (Service s : m.services) {
                if (s.category.equalsIgnoreCase(category)) {
                    System.out.println("🔹 " + m.name + " → " + s.name + " @ ₹" + s.price);
                }
            }
        }
    }

    Merchant findMerchantByName(String name) {
        return merchants.stream()
                .filter(m -> m.name.equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    void placeOrder(Customer c, Merchant m, String sname, Date appTime) {
        Optional<Service> service = m.getService(sname);
        if (service.isPresent()) {
            Order o = new Order(c, m, service.get(), appTime);
            c.addOrder(o);
            m.receiveOrder(o);
            processPayment(c, service.get().price);
            sendEmail(c.email, "Order Confirmation",
                    "Thanks " + c.name + ", your order for " + sname + " is confirmed.\nAppointment: " + appTime);
            sendEmail(m.email, "New Order Received",
                    "Hi " + m.name + ", you have a new order for " + sname + " from " + c.name + ".");
        } else {
            System.out.println("❌ Service not found.");
        }
    }

    void viewMerchantDashboard(Merchant m) {
        System.out.println("\n📊 Orders for " + m.name);
        for (Order o : m.orders)
            System.out.println("🛒 " + o.service.name + " booked by " + o.customer.name + " on " + o.appointment);
    }

    void viewCustomerOrders(Customer c) {
        System.out.println("\n📦 Your Orders:");
        for (Order o : c.orderHistory)
            System.out.println("🧾 " + o.service.name + " from " + o.merchant.name + " at " + o.appointment);
    }

    void processPayment(Customer c, double amt) {
        System.out.println("💳 Payment of ₹" + amt + " successful!");
    }

    void sendEmail(String to, String subject, String msg) {
        System.out.println("\n📧 Email to: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Message:\n" + msg);
    }
}