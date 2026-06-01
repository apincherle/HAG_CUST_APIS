package com.example.shopify.repository;

import com.example.shopify.entity.ShopifyWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopifyWebhookEventRepository extends JpaRepository<ShopifyWebhookEvent, String> {
}
