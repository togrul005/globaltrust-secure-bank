package com.globaltrust.bank.config;

import com.globaltrust.bank.domain.*;
import com.globaltrust.bank.repo.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Seeds a small, demo-ready data set on startup. Every account starts with a
 * round balance; every user shares the same demo password ("password") for ease
 * of demonstration — see the README. The role set is chosen to exercise both
 * the Clark-Wilson and the Chinese Wall scenarios end to end.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(AppUserRepository users,
                           AccountRepository accounts,
                           CompanyRepository companies,
                           PasswordEncoder encoder) {
        return args -> {
            String pw = encoder.encode("password");

            // --- users (RBAC) ---
            users.save(new AppUser("alice",  pw, "Alice Retail",     Role.CUSTOMER, "RETAIL"));
            users.save(new AppUser("bob",    pw, "Bob Retail",       Role.CUSTOMER, "RETAIL"));
            users.save(new AppUser("tara",   pw, "Tara Teller",      Role.TELLER,   "RETAIL_OPS"));
            users.save(new AppUser("victor", pw, "Victor Approver",  Role.APPROVER, "RETAIL_OPS"));
            users.save(new AppUser("wendy",  pw, "Wendy Approver",   Role.APPROVER, "RETAIL_OPS"));
            users.save(new AppUser("adam",   pw, "Adam Admin",       Role.ADMIN,    "IT"));
            users.save(new AppUser("aria",   pw, "Aria Auditor",     Role.AUDITOR,  "COMPLIANCE"));
            users.save(new AppUser("diana",  pw, "Diana Advisor",    Role.ADVISOR,  "ADVISORY"));
            users.save(new AppUser("trent",  pw, "Trent Trader",     Role.TRADER,   "TRADING"));

            // --- accounts (CDIs) ---
            accounts.save(new Account("ACC-1001", "alice", new BigDecimal("5000.00"),    "EUR"));
            accounts.save(new Account("ACC-1002", "bob",   new BigDecimal("8000.00"),    "EUR"));
            accounts.save(new Account("ACC-9000", "bank",  new BigDecimal("1000000.00"), "EUR")); // operations

            // --- client companies for the Chinese Wall (two CoI classes, two competitors each) ---
            companies.save(new Company("Acme Corp", "TECH",
                    "Pending acquisition of a rival; price-sensitive."));
            companies.save(new Company("Globex Inc", "TECH",
                    "Preparing a competing bid in the same sector."));   // competitor of Acme
            companies.save(new Company("Initech Labs", "PHARMA",
                    "Phase III trial results not yet public."));
            companies.save(new Company("Umbrella Pharma", "PHARMA",
                    "Considering a hostile takeover."));                 // competitor of Initech
        };
    }
}
