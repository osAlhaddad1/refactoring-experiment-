package com.example.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Black-box HTTP tests for the shop.
 *
 * They only look at HTTP status codes and JSON response bodies, never at
 * internal classes. They define "correct behaviour" and must keep passing both
 * before and after a refactoring. A refactoring that breaks them is disqualified.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShopBehaviourTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createAndReadProduct() throws Exception {
        String id = createProduct("Widget", 10.0, 100);

        mockMvc.perform(get("/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andExpect(jsonPath("$.price").value(10.0))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void smallOrderHasNoDiscount() throws Exception {
        String id = createProduct("Cup", 10.0, 100);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + id + ",\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.total").value(50.0));

        // stock went down by 5
        mockMvc.perform(get("/products/" + id))
                .andExpect(jsonPath("$.stock").value(95));
    }

    @Test
    void bulkOrderGetsTenPercentOff() throws Exception {
        String id = createProduct("Plate", 10.0, 100);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + id + ",\"quantity\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(90.0));
    }

    @Test
    void orderBiggerThanStockIsRejected() throws Exception {
        String id = createProduct("Spoon", 10.0, 3);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + id + ",\"quantity\":10}"))
                .andExpect(status().isBadRequest());
    }

    // ----- small helpers --------------------------------------------------

    /** Creates a product and returns its generated id as text. */
    private String createProduct(String name, double price, int stock) throws Exception {
        String json = "{\"name\":\"" + name + "\",\"price\":" + price + ",\"stock\":" + stock + "}";
        String body = mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        return readId(body);
    }

    /** Pulls the "id" field out of a JSON response body. */
    private String readId(String json) {
        try {
            JsonNode node = new ObjectMapper().readTree(json);
            return node.get("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("could not read id from response: " + json, e);
        }
    }
}
