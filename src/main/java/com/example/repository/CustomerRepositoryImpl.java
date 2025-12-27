package com.example.repository;

import com.example.model.Address;
import com.example.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Customer> findByCustomerIdNative(String customerIdString) {
        Query query = entityManager.createNativeQuery(
            "SELECT customer_id, email, phone, full_name, " +
            "billing_line1, billing_line2, billing_city, billing_region, billing_postcode, billing_country, " +
            "shipping_line1, shipping_line2, shipping_city, shipping_region, shipping_postcode, shipping_country, " +
            "marketing_opt_in, status, created_at, updated_at, deleted_at " +
            "FROM customers WHERE customer_id = ?", 
            Object[].class
        );
        query.setParameter(1, customerIdString);
        
        try {
            Object[] result = (Object[]) query.getSingleResult();
            if (result == null) {
                return Optional.empty();
            }
            
            Customer customer = mapResultToCustomer(result);
            return Optional.of(customer);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Customer> findByEmailNative(String email) {
        Query query = entityManager.createNativeQuery(
            "SELECT customer_id, email, phone, full_name, " +
            "billing_line1, billing_line2, billing_city, billing_region, billing_postcode, billing_country, " +
            "shipping_line1, shipping_line2, shipping_city, shipping_region, shipping_postcode, shipping_country, " +
            "marketing_opt_in, status, created_at, updated_at, deleted_at " +
            "FROM customers WHERE LOWER(email) = LOWER(?)", 
            Object[].class
        );
        query.setParameter(1, email);
        
        try {
            Object[] result = (Object[]) query.getSingleResult();
            if (result == null) {
                return Optional.empty();
            }
            
            Customer customer = mapResultToCustomer(result);
            return Optional.of(customer);
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }

    private Customer mapResultToCustomer(Object[] result) {
        Customer customer = new Customer();
        customer.setCustomerId(UUID.fromString((String) result[0]));
        customer.setEmail((String) result[1]);
        customer.setPhone((String) result[2]);
        customer.setFullName((String) result[3]);
        
        // Billing address
        Address billingAddress = new Address();
        billingAddress.setLine1((String) result[4]);
        billingAddress.setLine2((String) result[5]);
        billingAddress.setCity((String) result[6]);
        billingAddress.setRegion((String) result[7]);
        billingAddress.setPostcode((String) result[8]);
        billingAddress.setCountry((String) result[9]);
        customer.setBillingAddress(billingAddress);
        
        // Shipping address
        Address shippingAddress = new Address();
        shippingAddress.setLine1((String) result[10]);
        shippingAddress.setLine2((String) result[11]);
        shippingAddress.setCity((String) result[12]);
        shippingAddress.setRegion((String) result[13]);
        shippingAddress.setPostcode((String) result[14]);
        shippingAddress.setCountry((String) result[15]);
        customer.setShippingAddress(shippingAddress);
        
        customer.setMarketingOptIn((Boolean) result[16]);
        customer.setStatus(Customer.CustomerStatus.valueOf((String) result[17]));
        customer.setCreatedAt(((java.sql.Timestamp) result[18]).toLocalDateTime());
        customer.setUpdatedAt(((java.sql.Timestamp) result[19]).toLocalDateTime());
        if (result[20] != null) {
            customer.setDeletedAt(((java.sql.Timestamp) result[20]).toLocalDateTime());
        }
        
        return customer;
    }

    @Override
    public Page<Customer> findAllNative(Pageable pageable) {
        // Get total count
        Query countQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM customers WHERE status != 'DELETED'");
        Long total = ((Number) countQuery.getSingleResult()).longValue();
        
        // Build query with pagination
        String sql = "SELECT customer_id, email, phone, full_name, " +
            "billing_line1, billing_line2, billing_city, billing_region, billing_postcode, billing_country, " +
            "shipping_line1, shipping_line2, shipping_city, shipping_region, shipping_postcode, shipping_country, " +
            "marketing_opt_in, status, created_at, updated_at, deleted_at " +
            "FROM customers WHERE status != 'DELETED'";
        
        // Add sorting
        if (pageable.getSort().isSorted()) {
            String sortField = pageable.getSort().iterator().next().getProperty();
            String sortDirection = pageable.getSort().iterator().next().getDirection().name();
            
            // Map JPA field names to database column names
            String dbSortField = sortField;
            if ("createdAt".equals(sortField)) dbSortField = "created_at";
            else if ("fullName".equals(sortField)) dbSortField = "full_name";
            else if ("updatedAt".equals(sortField)) dbSortField = "updated_at";
            
            sql += " ORDER BY " + dbSortField + " " + sortDirection;
        } else {
            sql += " ORDER BY created_at DESC";
        }
        
        sql += " LIMIT ? OFFSET ?";
        
        Query query = entityManager.createNativeQuery(sql, Object[].class);
        query.setParameter(1, pageable.getPageSize());
        query.setParameter(2, pageable.getOffset());
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Customer> customers = new ArrayList<>();
        for (Object[] result : results) {
            customers.add(mapResultToCustomer(result));
        }
        
        return new PageImpl<>(customers, pageable, total);
    }

    @Override
    public Page<Customer> searchCustomersNative(String q, String email, String phone, Customer.CustomerStatus status, Pageable pageable) {
        // Build WHERE clause
        List<String> conditions = new ArrayList<>();
        conditions.add("status != 'DELETED'");
        
        if (q != null && !q.isEmpty()) {
            conditions.add("(LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR phone LIKE ?)");
        }
        if (email != null && !email.isEmpty()) {
            conditions.add("LOWER(email) = LOWER(?)");
        }
        if (phone != null && !phone.isEmpty()) {
            conditions.add("phone = ?");
        }
        if (status != null) {
            conditions.add("status = ?");
        }
        
        String whereClause = String.join(" AND ", conditions);
        
        // Get total count
        String countSql = "SELECT COUNT(*) FROM customers WHERE " + whereClause;
        Query countQuery = entityManager.createNativeQuery(countSql);
        int paramIndex = 1;
        if (q != null && !q.isEmpty()) {
            String searchPattern = "%" + q + "%";
            countQuery.setParameter(paramIndex++, searchPattern);
            countQuery.setParameter(paramIndex++, searchPattern);
            countQuery.setParameter(paramIndex++, searchPattern);
        }
        if (email != null && !email.isEmpty()) {
            countQuery.setParameter(paramIndex++, email);
        }
        if (phone != null && !phone.isEmpty()) {
            countQuery.setParameter(paramIndex++, phone);
        }
        if (status != null) {
            countQuery.setParameter(paramIndex++, status.name());
        }
        Long total = ((Number) countQuery.getSingleResult()).longValue();
        
        // Build query with pagination
        String sql = "SELECT customer_id, email, phone, full_name, " +
            "billing_line1, billing_line2, billing_city, billing_region, billing_postcode, billing_country, " +
            "shipping_line1, shipping_line2, shipping_city, shipping_region, shipping_postcode, shipping_country, " +
            "marketing_opt_in, status, created_at, updated_at, deleted_at " +
            "FROM customers WHERE " + whereClause;
        
        // Add sorting
        if (pageable.getSort().isSorted()) {
            String sortField = pageable.getSort().iterator().next().getProperty();
            String sortDirection = pageable.getSort().iterator().next().getDirection().name();
            
            String dbSortField = sortField;
            if ("createdAt".equals(sortField)) dbSortField = "created_at";
            else if ("fullName".equals(sortField)) dbSortField = "full_name";
            else if ("updatedAt".equals(sortField)) dbSortField = "updated_at";
            
            sql += " ORDER BY " + dbSortField + " " + sortDirection;
        } else {
            sql += " ORDER BY created_at DESC";
        }
        
        sql += " LIMIT ? OFFSET ?";
        
        Query query = entityManager.createNativeQuery(sql, Object[].class);
        paramIndex = 1;
        if (q != null && !q.isEmpty()) {
            String searchPattern = "%" + q + "%";
            query.setParameter(paramIndex++, searchPattern);
            query.setParameter(paramIndex++, searchPattern);
            query.setParameter(paramIndex++, searchPattern);
        }
        if (email != null && !email.isEmpty()) {
            query.setParameter(paramIndex++, email);
        }
        if (phone != null && !phone.isEmpty()) {
            query.setParameter(paramIndex++, phone);
        }
        if (status != null) {
            query.setParameter(paramIndex++, status.name());
        }
        query.setParameter(paramIndex++, pageable.getPageSize());
        query.setParameter(paramIndex, pageable.getOffset());
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        List<Customer> customers = new ArrayList<>();
        for (Object[] result : results) {
            customers.add(mapResultToCustomer(result));
        }
        
        return new PageImpl<>(customers, pageable, total);
    }
}

