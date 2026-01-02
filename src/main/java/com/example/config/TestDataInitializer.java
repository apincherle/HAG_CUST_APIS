package com.example.config;

import com.example.model.Address;
import com.example.model.Customer;
import com.example.model.Submission;
import com.example.model.SubmissionItem;
import com.example.model.SubmissionIntakeCode;
import com.example.qrcert.entity.CardCertificate;
import com.example.qrcert.entity.CardImage;
import com.example.repository.CustomerRepository;
import com.example.repository.SubmissionRepository;
import com.example.repository.SubmissionItemRepository;
import com.example.qrcert.repository.CardCertificateRepository;
import com.example.qrcert.repository.CardImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Service to initialize test data from CSV file.
 * Automatically runs on application startup and can also be called manually via REST endpoint.
 */
@Component
@org.springframework.context.annotation.Profile("dev")
public class TestDataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private SubmissionItemRepository submissionItemRepository;
    
    @Autowired
    private CardCertificateRepository cardCertificateRepository;
    
    @Autowired
    private CardImageRepository cardImageRepository;

    private boolean initialized = false;

    /**
     * Automatically initialize test data on application startup
     * Uses ContextRefreshedEvent to ensure all beans are ready
     */
    @Override
    @Transactional
    public void onApplicationEvent(@org.springframework.lang.NonNull ContextRefreshedEvent event) {
        // Only run once, even if context is refreshed multiple times
        if (initialized) {
            return;
        }
        initialized = true;
        
        System.out.println("TestDataInitializer: Checking and initializing test data on startup...");
        int customersCreated = initializeTestData();
        if (customersCreated > 0) {
            System.out.println("TestDataInitializer: Created " + customersCreated + " customer(s) on startup");
        } else {
            System.out.println("TestDataInitializer: All test customers already exist");
        }
        
        // Initialize submissions, items, and certificates
        initializeSubmissionsAndCertificates();
    }

    /**
     * Manual initialization method (can be called via REST endpoint)
     */
    @Transactional
    public int initializeTestData() {
        int created = 0;
        try {
            // Load CSV file from resources - try multiple locations
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("com/example/repository/customers.csv");
            
            if (inputStream == null) {
                inputStream = getClass().getResourceAsStream("/com/example/repository/customers.csv");
            }
            
            if (inputStream == null) {
                inputStream = getClass().getClassLoader().getResourceAsStream("customers.csv");
            }
            
            if (inputStream == null) {
                System.err.println("WARNING: customers.csv not found in any expected location");
                System.err.println("  Tried: com/example/repository/customers.csv");
                System.err.println("  Tried: /com/example/repository/customers.csv");
                System.err.println("  Tried: customers.csv");
                return 0;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    
                    String[] fields = line.split(",");
                    if (fields.length < 21) {
                        System.out.println("WARNING: Skipping invalid CSV line (expected 21 fields, got " + fields.length + "): " + line);
                        continue;
                    }
                    
                    // Parse CSV fields
                    String customerIdStr = fields[0].trim();
                    String billingCity = fields[1].trim();
                    String billingCountry = fields[2].trim();
                    String billingLine1 = fields[3].trim();
                    String billingLine2 = fields[4].trim();
                    String billingPostcode = fields[5].trim();
                    String billingRegion = fields[6].trim();
                    long createdAtMillis = Long.parseLong(fields[7].trim());
                    String deletedAtStr = fields[8].trim();
                    String email = fields[9].trim();
                    String fullName = fields[10].trim();
                    int marketingOptInInt = Integer.parseInt(fields[11].trim());
                    String phone = fields[12].trim();
                    String shippingCity = fields[13].trim();
                    String shippingCountry = fields[14].trim();
                    String shippingLine1 = fields[15].trim();
                    String shippingLine2 = fields[16].trim();
                    String shippingPostcode = fields[17].trim();
                    String shippingRegion = fields[18].trim();
                    String statusStr = fields[19].trim();
                    long updatedAtMillis = Long.parseLong(fields[20].trim());
                    
                    UUID customerId = UUID.fromString(customerIdStr);
                    
                    // Check if customer already exists
                    if (customerRepository.findByCustomerIdNative(customerIdStr).isPresent()) {
                        System.out.println("Customer already exists: " + customerIdStr);
                        continue;
                    }
                    
                    // Create customer
                    Customer customer = new Customer();
                    customer.setCustomerId(customerId);
                    customer.setEmail(email.toLowerCase());
                    customer.setPhone(phone.isEmpty() ? null : phone);
                    customer.setFullName(fullName);
                    customer.setMarketingOptIn(marketingOptInInt == 1);
                    customer.setStatus(Customer.CustomerStatus.valueOf(statusStr));
                    
                    // Set billing address
                    Address billingAddress = new Address();
                    billingAddress.setLine1(billingLine1.isEmpty() ? null : billingLine1);
                    billingAddress.setLine2(billingLine2.isEmpty() ? null : billingLine2);
                    billingAddress.setCity(billingCity.isEmpty() ? null : billingCity);
                    billingAddress.setRegion(billingRegion.isEmpty() ? null : billingRegion);
                    billingAddress.setPostcode(billingPostcode.isEmpty() ? null : billingPostcode);
                    billingAddress.setCountry(billingCountry.isEmpty() ? null : billingCountry);
                    customer.setBillingAddress(billingAddress);
                    
                    // Set shipping address
                    Address shippingAddress = new Address();
                    shippingAddress.setLine1(shippingLine1.isEmpty() ? null : shippingLine1);
                    shippingAddress.setLine2(shippingLine2.isEmpty() ? null : shippingLine2);
                    shippingAddress.setCity(shippingCity.isEmpty() ? null : shippingCity);
                    shippingAddress.setRegion(shippingRegion.isEmpty() ? null : shippingRegion);
                    shippingAddress.setPostcode(shippingPostcode.isEmpty() ? null : shippingPostcode);
                    shippingAddress.setCountry(shippingCountry.isEmpty() ? null : shippingCountry);
                    customer.setShippingAddress(shippingAddress);
                    
                    // Set timestamps
                    LocalDateTime createdAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(createdAtMillis), ZoneId.systemDefault());
                    LocalDateTime updatedAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(updatedAtMillis), ZoneId.systemDefault());
                    customer.setCreatedAt(createdAt);
                    customer.setUpdatedAt(updatedAt);
                    
                    if (!deletedAtStr.isEmpty()) {
                        long deletedAtMillis = Long.parseLong(deletedAtStr);
                        LocalDateTime deletedAt = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(deletedAtMillis), ZoneId.systemDefault());
                        customer.setDeletedAt(deletedAt);
                    }
                    
                    // Save customer
                    customerRepository.save(customer);
                    System.out.println("Created customer: " + customerIdStr + " (" + email + ")");
                    created++;
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize test data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize test data", e);
        }
        return created;
    }
    
    /**
     * Initialize test submissions, submission items, and certificates
     */
    @Transactional
    public void initializeSubmissionsAndCertificates() {
        // Get active customers
        List<Customer> customers = customerRepository.findAll().stream()
            .filter(c -> c.getStatus() == Customer.CustomerStatus.ACTIVE && c.getDeletedAt() == null)
            .limit(5) // Use first 5 active customers
            .toList();
        
        if (customers.isEmpty()) {
            System.out.println("TestDataInitializer: No active customers found, skipping submission/certificate initialization");
            return;
        }
        
        int submissionsCreated = 0;
        int itemsCreated = 0;
        int certificatesCreated = 0;
        
        // Create submissions for each customer
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            
            // Check if submissions already exist for this customer
            if (submissionRepository.countByCustomerId(customer.getCustomerId()) > 0) {
                continue;
            }
            
            // Create 1-3 submissions per customer
            int numSubmissions = (i % 3) + 1;
            
            // Use different statuses to ensure we have some graded submissions
            Submission.SubmissionStatus[] statuses = {
                Submission.SubmissionStatus.SUBMITTED_RECEIVED,
                Submission.SubmissionStatus.GRADING_STARTED,
                Submission.SubmissionStatus.GRADED,
                Submission.SubmissionStatus.FINALISED,
                Submission.SubmissionStatus.POSTED
            };
            
            for (int j = 0; j < numSubmissions; j++) {
                Submission submission = new Submission();
                submission.setCustomerId(customer.getCustomerId());
                submission.setSubmissionNumber("SUB-" + customer.getCustomerId().toString().substring(0, 8).toUpperCase() + "-" + String.format("%03d", j + 1));
                submission.setServiceLevel(Submission.ServiceLevel.values()[j % Submission.ServiceLevel.values().length]);
                submission.setStatus(statuses[j % statuses.length]);
                submission.setNotesCustomer("Test submission " + (j + 1) + " for customer " + customer.getFullName());
                submission.setCreatedAt(LocalDateTime.now().minusDays(30 - (j * 5)));
                submission.setUpdatedAt(LocalDateTime.now().minusDays(30 - (j * 5)));
                
                // Create intake code
                SubmissionIntakeCode intakeCode = new SubmissionIntakeCode();
                intakeCode.setSubmission(submission);
                intakeCode.setValue("INTAKE-" + submission.getSubmissionNumber());
                intakeCode.setBarcodeFormat(SubmissionIntakeCode.BarcodeFormat.CODE_128);
                intakeCode.setQrValue("https://hags-grading.co.uk/submission/" + submission.getSubmissionNumber());
                submission.setIntakeCode(intakeCode);
                
                submission = submissionRepository.save(submission);
                submissionsCreated++;
                
                // Create 2-4 items per submission
                int numItems = 2 + (j % 3);
                
                for (int k = 0; k < numItems; k++) {
                    SubmissionItem item = new SubmissionItem();
                    item.setSubmission(submission);
                    item.setLineNumber(k + 1);
                    item.setGame(SubmissionItem.GameType.values()[k % SubmissionItem.GameType.values().length]);
                    
                    // Create realistic card descriptions
                    String[] cardNames = {
                        "Charizard", "Pikachu", "Blastoise", "Venusaur", "Mewtwo",
                        "Black Lotus", "Ancestral Recall", "Time Walk", "Mox Pearl", "Mox Sapphire",
                        "Michael Jordan", "LeBron James", "Tom Brady", "Wayne Gretzky", "Babe Ruth"
                    };
                    String[] setNames = {
                        "Base Set", "Jungle", "Fossil", "Team Rocket",
                        "Alpha", "Beta", "Unlimited", "Revised",
                        "Topps", "Upper Deck", "Panini", "Fleer"
                    };
                    
                    String cardName = cardNames[(k + j) % cardNames.length];
                    String setName = setNames[(k + j) % setNames.length];
                    int year = 1990 + ((k + j) % 30);
                    
                    item.setFreeTextLine(cardName + " - " + setName + " (" + year + ") #" + (k + 1));
                    item.setCustomerNotes("Please grade carefully");
                    item.setRequestedPhotoSlots(2);
                    item.setFrontPhotoId("photo-front-" + item.getItemId());
                    item.setBackPhotoId("photo-back-" + item.getItemId());
                    item.setEnrichmentStatus(SubmissionItem.EnrichmentStatus.values()[k % SubmissionItem.EnrichmentStatus.values().length]);
                    item.setEnrichmentConfidence(0.85 + (k * 0.05));
                    item.setMatchedCatalogId("CAT-" + cardName.toUpperCase().replace(" ", "-") + "-" + year);
                    
                    item = submissionItemRepository.save(item);
                    itemsCreated++;
                    
                    // Create certificate for some items (about 50% of items) if submission is graded
                    // Also create for FINALISED and POSTED statuses
                    boolean shouldCreateCertificate = (k % 2 == 0) && 
                        (submission.getStatus() == Submission.SubmissionStatus.GRADED ||
                         submission.getStatus() == Submission.SubmissionStatus.FINALISED ||
                         submission.getStatus() == Submission.SubmissionStatus.POSTED);
                    
                    if (shouldCreateCertificate) {
                        // Check if certificate already exists for this item
                        String itemIdStr = item.getItemId().toString();
                        boolean certificateExists = cardCertificateRepository.findAll().stream()
                            .anyMatch(cert -> cert.getItemId().equals(itemIdStr));
                        
                        if (!certificateExists) {
                            String publicId = generatePublicId();
                            // Ensure public ID is unique
                            while (cardCertificateRepository.existsByPublicId(publicId)) {
                                publicId = generatePublicId();
                            }
                            
                            CardCertificate certificate = CardCertificate.builder()
                                .publicId(publicId)
                                .serialNumber("HAGS-" + year + "-" + String.format("%06d", certificatesCreated + 1))
                                .submissionId(submission.getSubmissionId().toString())
                                .customerId(customer.getCustomerId().toString())
                                .itemId(itemIdStr)
                                .status("VERIFIED")
                                .cardName(cardName)
                                .setName(setName)
                                .year(year)
                                .cardNumber(String.valueOf(k + 1))
                                .variant("Standard")
                                .grade(8.5 + (k * 0.5))
                                .graderVersion("v2.1")
                                .gradedAt(LocalDateTime.now().minusDays(10 - k))
                                .notesPublic("Excellent condition")
                                .notesInternal("Minor edge wear")
                                .checksumSha256("a1b2c3d4e5f6" + String.format("%052d", certificatesCreated))
                                .build();
                        
                        // Add images
                        CardImage frontImage = CardImage.builder()
                            .certificate(certificate)
                            .kind("front")
                            .url("https://example.com/images/" + certificate.getPublicId() + "/front.jpg")
                            .width(1200)
                            .height(1680)
                            .build();
                        
                        CardImage backImage = CardImage.builder()
                            .certificate(certificate)
                            .kind("back")
                            .url("https://example.com/images/" + certificate.getPublicId() + "/back.jpg")
                            .width(1200)
                            .height(1680)
                            .build();
                        
                            certificate.getImages().add(frontImage);
                            certificate.getImages().add(backImage);
                            
                            certificate = cardCertificateRepository.save(certificate);
                            certificatesCreated++;
                        }
                    }
                }
            }
        }
        
        if (submissionsCreated > 0 || itemsCreated > 0 || certificatesCreated > 0) {
            System.out.println("TestDataInitializer: Created " + submissionsCreated + " submission(s), " + 
                             itemsCreated + " item(s), and " + certificatesCreated + " certificate(s)");
        } else {
            System.out.println("TestDataInitializer: All test submissions/items/certificates already exist");
        }
    }
    
    /**
     * Generates a random public ID for certificates (16 hex characters)
     */
    private String generatePublicId() {
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            sb.append(Integer.toHexString(random.nextInt(16)));
        }
        return sb.toString().toUpperCase();
    }
}

