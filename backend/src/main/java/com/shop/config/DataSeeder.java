package com.shop.config;

import com.shop.model.*;
import com.shop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates the admin account on first run, and (when app.seed-demo-data=true, the default
 * for local development) a set of categories and affordably priced sample products so the
 * storefront isn't empty. Safe to run on every startup: it checks for existing rows first.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}") private String adminEmail;
    @Value("${app.admin.password}") private String adminPassword;
    @Value("${app.seed-demo-data:true}") private boolean seedDemoData;

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();
        if (seedDemoData) seedCatalog();
    }

    private void seedAdmin() {
        if (userRepository.existsByEmail(adminEmail)) return;
        User admin = new User();
        admin.setName("Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        Cart cart = new Cart();
        cart.setUser(admin);
        cartRepository.save(cart);
    }

    private void seedCatalog() {
        if (categoryRepository.count() > 0) return;   // already seeded

        Map<String, Category> cats = new LinkedHashMap<>();
        String[][] categories = {
            {"Electronics", "Reliable sound and everyday tech at a friendly price."},
            {"Mobiles & Accessories", "Keep your phone charged, protected and ready."},
            {"Men's Fashion", "Comfortable everyday style, made to last."},
            {"Women's Fashion", "Trendy picks that look great and cost less."},
            {"Home & Kitchen", "Smart essentials that make home life easier."},
            {"Beauty & Care", "Gentle, skin-friendly care for daily use."},
            {"Books & Stationery", "Great for school, office and quiet evenings."},
            {"Sports & Fitness", "Get moving with gear built for daily workouts."},
            {"Toys & Games", "Fun that keeps kids playing and learning."},
            {"Grocery", "Fresh, quality staples for your kitchen."},
        };
        for (String[] c : categories) {
            Category cat = new Category();
            cat.setName(c[0]);
            cat.setDescription(c[1]);
            cats.put(c[0], categoryRepository.save(cat));
        }

        // {name, price, stock, category}
        Object[][] products = {
            {"Wired Earphones with Mic", "299.00", 40, "Electronics"},
            {"Bluetooth Speaker", "799.00", 25, "Electronics"},
            {"USB Desk Fan", "349.00", 30, "Electronics"},
            {"LED Desk Lamp", "449.00", 20, "Electronics"},
            {"20W Fast Charger", "399.00", 50, "Mobiles & Accessories"},
            {"Tempered Glass Guard", "99.00", 100, "Mobiles & Accessories"},
            {"Silicone Phone Cover", "149.00", 60, "Mobiles & Accessories"},
            {"Power Bank 10000mAh", "899.00", 35, "Mobiles & Accessories"},
            {"Cotton T-Shirt", "249.00", 80, "Men's Fashion"},
            {"Slim Fit Jeans", "799.00", 40, "Men's Fashion"},
            {"Running Shoes", "999.00", 30, "Men's Fashion"},
            {"Analog Watch", "599.00", 25, "Men's Fashion"},
            {"Cotton Kurti", "449.00", 35, "Women's Fashion"},
            {"Everyday Handbag", "599.00", 20, "Women's Fashion"},
            {"UV Sunglasses", "299.00", 45, "Women's Fashion"},
            {"Fashion Earrings Set", "199.00", 55, "Women's Fashion"},
            {"Steel Water Bottle", "249.00", 70, "Home & Kitchen"},
            {"Non-stick Pan", "599.00", 22, "Home & Kitchen"},
            {"Double Bedsheet Set", "499.00", 18, "Home & Kitchen"},
            {"Storage Container Set", "349.00", 40, "Home & Kitchen"},
            {"Face Wash 100ml", "149.00", 90, "Beauty & Care"},
            {"Matte Lipstick", "199.00", 60, "Beauty & Care"},
            {"Hair Oil 200ml", "179.00", 75, "Beauty & Care"},
            {"Body Mist Perfume", "299.00", 30, "Beauty & Care"},
            {"Notebook Pack of 5", "199.00", 65, "Books & Stationery"},
            {"Gel Pens Set of 10", "99.00", 120, "Books & Stationery"},
            {"Bestseller Novel", "249.00", 40, "Books & Stationery"},
            {"Daily Study Planner", "149.00", 30, "Books & Stationery"},
            {"Yoga Mat 6mm", "399.00", 28, "Sports & Fitness"},
            {"Tennis Cricket Bat", "499.00", 15, "Sports & Fitness"},
            {"Skipping Rope", "149.00", 50, "Sports & Fitness"},
            {"Football Size 5", "349.00", 24, "Sports & Fitness"},
            {"Soft Teddy Bear", "299.00", 32, "Toys & Games"},
            {"Building Blocks 200pcs", "399.00", 26, "Toys & Games"},
            {"Remote Control Car", "599.00", 18, "Toys & Games"},
            {"Ludo Board Game", "199.00", 40, "Toys & Games"},
            {"Almonds 250g", "229.00", 55, "Grocery"},
            {"Honey 500g", "249.00", 45, "Grocery"},
            {"Green Tea 25 Bags", "149.00", 60, "Grocery"},
            {"Dark Chocolate 100g", "129.00", 70, "Grocery"},
        };
        for (Object[] row : products) {
            Product p = new Product();
            p.setName((String) row[0]);
            p.setDescription(row[0] + ". A popular pick in " + row[3] + ".");
            p.setPrice(new BigDecimal((String) row[1]));
            p.setStock((Integer) row[2]);
            p.setCategory(cats.get(row[3]));
            productRepository.save(p);
        }
    }
}
