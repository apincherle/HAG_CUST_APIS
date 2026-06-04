package com.example.shopify.support;

import com.example.shopify.dto.ShopifyPropertyPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GloboCardCaptureTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parseSlots_fiveCardBundle() {
        List<ShopifyPropertyPayload> props = List.of(
                prop("cardname-1", "Charizard"),
                prop("notes-1", "Holo swirl"),
                prop("card-front-1", "https://cdn.example/front1.jpg"),
                prop("card-back-1", "https://cdn.example/back1.jpg"),
                prop("cardname-2", "Blastoise"),
                prop("notes-2", "PSA candidate"),
                prop("card-front-2", "https://cdn.example/front2.jpg"),
                prop("card-back-2", "https://cdn.example/back2.jpg"),
                prop("cardname-3", "Venusaur"),
                prop("notes-3", ""),
                prop("card-front-3", "https://cdn.example/front3.jpg"),
                prop("card-back-3", "https://cdn.example/back3.jpg"),
                prop("cardname-4", "Pikachu"),
                prop("notes-4", "Yellow cheeks"),
                prop("card-front-4", "https://cdn.example/front4.jpg"),
                prop("card-back-4", "https://cdn.example/back4.jpg"),
                prop("cardname-5", "Mewtwo"),
                prop("notes-5", "1st edition"),
                prop("card-front-5", "https://cdn.example/front5.jpg"),
                prop("card-back-5", "https://cdn.example/back5.jpg")
        );

        List<GloboCardCapture.GloboCardSlot> slots = GloboCardCapture.parseSlots(props);
        assertEquals(5, slots.size());
        assertEquals(1, slots.get(0).slot());
        assertEquals("Charizard", slots.get(0).cardname());
        assertEquals("https://cdn.example/front5.jpg", slots.get(4).cardFrontUrl());
    }

    @Test
    void parseSlots_normalizesSpacesAndCase() {
        List<ShopifyPropertyPayload> props = List.of(
                prop("Cardname 1", "Mew"),
                prop("CARD-FRONT-1", "https://cdn.example/front.jpg")
        );

        List<GloboCardCapture.GloboCardSlot> slots = GloboCardCapture.parseSlots(props);
        assertEquals(1, slots.size());
        assertEquals("Mew", slots.get(0).cardname());
        assertEquals("https://cdn.example/front.jpg", slots.get(0).cardFrontUrl());
    }

    @Test
    void toLineJson_structuredOutput() throws Exception {
        var line = new com.example.shopify.dto.ShopifyLineItemPayload();
        line.setId(99L);
        line.setSku("HAGS-SUB-GOLD");
        line.setProperties(List.of(
                prop("cardname-1", "Lugia"),
                prop("card-back-1", "https://cdn.example/back.jpg")
        ));

        String json = GloboCardCapture.toLineJson(line, mapper);
        assertNotNull(json);
        assertTrue(json.contains("\"cardname\":\"Lugia\""));
        assertTrue(json.contains("\"card_back_url\""));
    }

    private static ShopifyPropertyPayload prop(String name, String value) {
        ShopifyPropertyPayload p = new ShopifyPropertyPayload();
        p.setName(name);
        p.setValue(value);
        return p;
    }
}
