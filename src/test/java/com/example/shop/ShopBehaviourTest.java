package com.example.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Black-box HTTP tests for the complex baseline. They only look at HTTP status
 * codes and JSON response bodies, never at internal classes, so they describe
 * "correct behaviour" and must keep passing before and after a refactoring.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShopBehaviourTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void customerStartsWithZeroLoyalty() throws Exception {
        long id = createCustomer("Alice");
        assertEquals(0, getOk("/customers/" + id).get("loyaltyPoints").asInt());
    }

    @Test
    void smallOrderHasNoDiscountAndReducesStock() throws Exception {
        long customer = createCustomer("Bob");
        long product = createProduct("Cup", 10.0, 100);

        JsonNode order = placeOrder(customer, product, 5);
        assertEquals("NEW", order.get("status").asText());
        assertEquals(50.0, order.get("total").asDouble());
        assertEquals(50.0, order.get("lines").get(0).get("linePrice").asDouble());

        assertEquals(95, getOk("/products/" + product).get("stock").asInt());
    }

    @Test
    void bulkLineGetsTenPercentOff() throws Exception {
        long customer = createCustomer("Cara");
        long product = createProduct("Plate", 10.0, 100);

        JsonNode order = placeOrder(customer, product, 10);
        assertEquals(90.0, order.get("lines").get(0).get("linePrice").asDouble());
        assertEquals(90.0, order.get("total").asDouble());
    }

    @Test
    void bigOrderGetsOrderLevelDiscount() throws Exception {
        long customer = createCustomer("Dan");
        long product = createProduct("Chair", 10.0, 100);

        // 60 items: line = 600 -> 10% off = 540; order subtotal 540 >= 500 -> 5% off = 513
        JsonNode order = placeOrder(customer, product, 60);
        assertEquals(540.0, order.get("lines").get(0).get("linePrice").asDouble());
        assertEquals(513.0, order.get("total").asDouble());
    }

    @Test
    void payingAdvancesStateAndEarnsLoyalty() throws Exception {
        long customer = createCustomer("Eve");
        long product = createProduct("Spoon", 10.0, 100);
        long orderId = placeOrder(customer, product, 5).get("id").asLong();

        JsonNode paid = action("/orders/" + orderId + "/pay");
        assertEquals("PAID", paid.get("status").asText());
        // total was 50 -> 50 loyalty points
        assertEquals(50, getOk("/customers/" + customer).get("loyaltyPoints").asInt());
    }

    @Test
    void shippingRequiresAPaidOrder() throws Exception {
        long customer = createCustomer("Finn");
        long product = createProduct("Fork", 10.0, 100);
        long orderId = placeOrder(customer, product, 5).get("id").asLong();

        // a NEW order cannot be shipped
        mockMvc.perform(post("/orders/" + orderId + "/ship")).andExpect(status().isBadRequest());

        // pay, then ship
        action("/orders/" + orderId + "/pay");
        assertEquals("SHIPPED", action("/orders/" + orderId + "/ship").get("status").asText());
    }

    @Test
    void cancellingRestocksTheItems() throws Exception {
        long customer = createCustomer("Gwen");
        long product = createProduct("Mug", 10.0, 100);
        long orderId = placeOrder(customer, product, 5).get("id").asLong();
        assertEquals(95, getOk("/products/" + product).get("stock").asInt());

        JsonNode cancelled = action("/orders/" + orderId + "/cancel");
        assertEquals("CANCELLED", cancelled.get("status").asText());
        assertEquals(100, getOk("/products/" + product).get("stock").asInt());
    }

    @Test
    void orderBiggerThanStockIsRejected() throws Exception {
        long customer = createCustomer("Hank");
        long product = createProduct("Pan", 10.0, 3);

        String body = "{\"customerId\":" + customer
                + ",\"lines\":[{\"productId\":" + product + ",\"quantity\":10}]}";
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void auditLogRecordsEvents() throws Exception {
        long customer = createCustomer("Ivy");
        long product = createProduct("Lamp", 10.0, 100);
        long orderId = placeOrder(customer, product, 5).get("id").asLong();
        action("/orders/" + orderId + "/pay");

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("created order " + orderId)))
                .andExpect(content().string(containsString("paid order " + orderId)));
    }

    // ----- helpers --------------------------------------------------------

    private long createCustomer(String name) throws Exception {
        return postOk("/customers", "{\"name\":\"" + name + "\"}").get("id").asLong();
    }

    private long createProduct(String name, double price, int stock) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"price\":" + price + ",\"stock\":" + stock + "}";
        return postOk("/products", body).get("id").asLong();
    }

    private JsonNode placeOrder(long customerId, long productId, int quantity) throws Exception {
        String body = "{\"customerId\":" + customerId
                + ",\"lines\":[{\"productId\":" + productId + ",\"quantity\":" + quantity + "}]}";
        return postOk("/orders", body);
    }

    /** POSTs a JSON body, asserts 200, returns the parsed response. */
    private JsonNode postOk(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }

    /** POSTs with no body (the state-machine actions), asserts 200, returns the response. */
    private JsonNode action(String url) throws Exception {
        String response = mockMvc.perform(post(url))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }

    private JsonNode getOk(String url) throws Exception {
        String response = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }
}
