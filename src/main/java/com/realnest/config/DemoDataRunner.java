package com.realnest.config;

import com.realnest.property.Property;
import com.realnest.property.PropertyRepository;
import com.realnest.property.PropertyType;
import com.realnest.user.Role;
import com.realnest.user.User;
import com.realnest.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoDataRunner implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PropertyRepository propertyRepository;
  private final PasswordEncoder passwordEncoder;

  public DemoDataRunner(
      UserRepository userRepository,
      PropertyRepository propertyRepository,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.propertyRepository = propertyRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (propertyRepository.count() > 0) {
      return;
    }
    User customer =
        userRepository
            .findByEmail("buyer@realnest.com")
            .orElseGet(
                () -> {
                  User user = new User();
                  user.setName("Buyer");
                  user.setEmail("buyer@realnest.com");
                  user.setPassword(passwordEncoder.encode("password"));
                  user.setRole(Role.CUSTOMER);
                  return userRepository.save(user);
                });
    User admin =
        userRepository
            .findByEmail("admin3112@gmail.com")
            .orElseGet(
                () -> {
                  User user = new User();
                  user.setName("Admin");
                  user.setEmail("admin3112@gmail.com");
                  user.setPassword(passwordEncoder.encode("nextnext"));
                  user.setRole(Role.ADMIN);
                  return userRepository.save(user);
                });

    List<Property> samples =
        List.of(
            build(
                "Modern Villa",
                "5 bed villa with pool",
                "Chennai",
                PropertyType.SALE,
                35000000d,
                "/img/featured-1.jpg",
                admin,
                true),
            build(
                "Beach House",
                "Sea view property",
                "Goa",
                PropertyType.SALE,
                27000000d,
                "/img/featured-2.jpg",
                customer,
                true),
            build(
                "City Apartment",
                "2 BHK in city center",
                "Mumbai",
                PropertyType.RENT,
                95000d,
                "/img/featured-3.jpg",
                customer,
                true),
            build(
                "Countryside Farm",
                "Acres of greenery",
                "Coimbatore",
                PropertyType.SALE,
                18000000d,
                "/img/featured-4.jpg",
                customer,
                false),
            build(
                "Studio Loft",
                "Perfect for singles",
                "Bengaluru",
                PropertyType.RENT,
                48000d,
                "/img/featured-5.jpg",
                customer,
                false),
            build(
                "Luxury Penthouse",
                "Skyline views",
                "Hyderabad",
                PropertyType.SALE,
                42000000d,
                "/img/featured-1.jpg",
                admin,
                false),
            build(
                "Suburban Home",
                "Family friendly",
                "Pune",
                PropertyType.SALE,
                15000000d,
                "/img/featured-2.jpg",
                customer,
                false),
            build(
                "Tech Park Condo",
                "Near IT corridor",
                "Chennai",
                PropertyType.RENT,
                65000d,
                "/img/featured-3.jpg",
                customer,
                false));

    propertyRepository.saveAll(samples);
  }

  private Property build(
      String title,
      String description,
      String location,
      PropertyType type,
      double price,
      String imageUrl,
      User owner,
      boolean approved) {
    Property property = new Property();
    property.setTitle(title);
    property.setDescription(description);
    property.setLocation(location);
    property.setType(type);
    property.setPrice(BigDecimal.valueOf(price));
    property.setImageUrl(imageUrl);
    property.setOwner(owner);
    property.setApproved(approved);
    return property;
  }
}
