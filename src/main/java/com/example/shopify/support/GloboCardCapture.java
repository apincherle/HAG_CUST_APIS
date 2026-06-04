package com.example.shopify.support;

import com.example.shopify.dto.ShopifyLineItemPayload;
import com.example.shopify.dto.ShopifyOrderPayload;
import com.example.shopify.dto.ShopifyPropertyPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Globo Product Options fields from Shopify line item properties / note_attributes.
 * <p>
 * Expected property names (per card slot N): {@code cardname-N}, {@code notes-N},
 * {@code card-front-N} (file URL), {@code card-back-N} (file URL).
 * Bundled products use N=1..5 (or higher if present in the payload).
 */
public final class GloboCardCapture {

    /** Globo bundled grading products commonly use five card slots. */
    public static final int DEFAULT_MAX_SLOT = 5;

    private static final Pattern GLOBO_FIELD = Pattern.compile(
            "^(cardname|notes|card-front|card-back)-(\\d+)$",
            Pattern.CASE_INSENSITIVE);

    private GloboCardCapture() {
    }

    public record GloboCardSlot(
            int slot,
            String cardname,
            String notes,
            String cardFrontUrl,
            String cardBackUrl) {

        boolean hasAnyValue() {
            return cardname != null || notes != null || cardFrontUrl != null || cardBackUrl != null;
        }
    }

    public record GloboLineCards(
            Long lineItemId,
            String sku,
            String title,
            List<GloboCardSlot> cards) {
    }

    public static String toOrderJson(ShopifyOrderPayload order, ObjectMapper mapper) {
        if (order == null) {
            return null;
        }
        ObjectNode root = mapper.createObjectNode();
        ArrayNode lines = mapper.createArrayNode();
        boolean any = false;

        if (order.getLineItems() != null) {
            for (ShopifyLineItemPayload line : order.getLineItems()) {
                List<GloboCardSlot> cards = parseSlots(line.getProperties());
                if (cards.isEmpty()) {
                    continue;
                }
                lines.add(lineNode(mapper, line.getId(), line.getSku(), line.getTitle(), cards));
                any = true;
            }
        }

        List<GloboCardSlot> checkoutCards = parseSlots(order.getNoteAttributes());
        if (!checkoutCards.isEmpty()) {
            ObjectNode checkout = mapper.createObjectNode();
            checkout.put("source", "note_attributes");
            checkout.set("cards", slotsArray(mapper, checkoutCards));
            root.set("checkout", checkout);
            any = true;
        }

        if (!lines.isEmpty()) {
            root.set("line_items", lines);
            any = true;
        }
        return any ? root.toString() : null;
    }

    public static String toLineJson(ShopifyLineItemPayload line, ObjectMapper mapper) {
        if (line == null) {
            return null;
        }
        List<GloboCardSlot> cards = parseSlots(line.getProperties());
        if (cards.isEmpty()) {
            return null;
        }
        return lineNode(mapper, line.getId(), line.getSku(), line.getTitle(), cards).toString();
    }

    public static List<GloboCardSlot> parseSlots(List<ShopifyPropertyPayload> properties) {
        Map<Integer, MutableSlot> bySlot = new LinkedHashMap<>();
        if (properties == null) {
            return List.of();
        }
        for (ShopifyPropertyPayload prop : properties) {
            if (prop == null || prop.getName() == null) {
                continue;
            }
            Matcher matcher = GLOBO_FIELD.matcher(normalizeName(prop.getName()));
            if (!matcher.matches()) {
                continue;
            }
            int slot = Integer.parseInt(matcher.group(2));
            String field = matcher.group(1).toLowerCase(Locale.ROOT);
            String value = blankToNull(prop.getValue());
            MutableSlot slotData = bySlot.computeIfAbsent(slot, MutableSlot::new);
            switch (field) {
                case "cardname" -> slotData.cardname = value;
                case "notes" -> slotData.notes = value;
                case "card-front" -> slotData.cardFrontUrl = value;
                case "card-back" -> slotData.cardBackUrl = value;
                default -> { /* no-op */ }
            }
        }
        return bySlot.values().stream()
                .sorted(Comparator.comparingInt(s -> s.slot))
                .map(MutableSlot::toRecord)
                .filter(GloboCardSlot::hasAnyValue)
                .toList();
    }

    private static ObjectNode lineNode(
            ObjectMapper mapper,
            Long lineItemId,
            String sku,
            String title,
            List<GloboCardSlot> cards) {
        ObjectNode node = mapper.createObjectNode();
        if (lineItemId != null) {
            node.put("line_item_id", lineItemId);
        }
        if (sku != null) {
            node.put("sku", sku);
        }
        if (title != null) {
            node.put("title", title);
        }
        node.set("cards", slotsArray(mapper, cards));
        return node;
    }

    private static ArrayNode slotsArray(ObjectMapper mapper, List<GloboCardSlot> cards) {
        ArrayNode array = mapper.createArrayNode();
        for (GloboCardSlot card : cards) {
            ObjectNode slot = mapper.createObjectNode();
            slot.put("slot", card.slot());
            if (card.cardname() != null) {
                slot.put("cardname", card.cardname());
            }
            if (card.notes() != null) {
                slot.put("notes", card.notes());
            }
            if (card.cardFrontUrl() != null) {
                slot.put("card_front_url", card.cardFrontUrl());
            }
            if (card.cardBackUrl() != null) {
                slot.put("card_back_url", card.cardBackUrl());
            }
            array.add(slot);
        }
        return array;
    }

    /** Globo may send "Cardname 1" or "cardname-1"; normalize to hyphenated lowercase. */
    static String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace(' ', '-');
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class MutableSlot {
        final int slot;
        String cardname;
        String notes;
        String cardFrontUrl;
        String cardBackUrl;

        MutableSlot(int slot) {
            this.slot = slot;
        }

        GloboCardSlot toRecord() {
            return new GloboCardSlot(slot, cardname, notes, cardFrontUrl, cardBackUrl);
        }
    }
}
