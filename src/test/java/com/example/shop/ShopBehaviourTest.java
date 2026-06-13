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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Black-box HTTP tests for the xcomplex baseline. They only look at HTTP status
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
    void productKeepsItsCategory() throws Exception {
        long category = createCategory("Kitchen");
        long product = createProduct("Pot", 10.0, 50, category);

        JsonNode fetched = getOk("/products/" + product);
        assertEquals("Kitchen", fetched.get("category").get("name").asText());
    }

    @Test
    void smallOrderHasNoDiscountAndReducesStock() throws Exception {
        long customer = createCustomer("Bob");
        long product = createProduct("Cup", 10.0, 100, createCategory("C1"));

        JsonNode order = placeOrder(customer, product, 5);
        assertEquals("NEW", order.get("status").asText());
        assertEquals(50.0, order.get("total").asDouble());

        assertEquals(95, getOk("/products/" + product).get("stock").asInt());
    }

    @Test
    void bulkLineGetsTenPercentOff() throws Exception {
        long customer = createCustomer("Cara");
        long product = createProduct("Plate", 10.0, 100, createCategory("C2"));

        JsonNode order = placeOrder(customer, product, 10);
        assertEquals(90.0, order.get("total").asDouble());
    }

    @Test
    void bigOrderGetsOrderLevelDiscount() throws Exception {
        long customer = createCustomer("Dan");
        long product = createProduct("Chair", 10.0, 100, createCategory("C3"));

        JsonNode order = placeOrder(customer, product, 60);
        assertEquals(513.0, order.get("total").asDouble());
    }

    @Test
    void couponGivesAnExtraDiscount() throws Exception {
        long customer = createCustomer("Eve");
        long product = createProduct("Spoon", 10.0, 100, createCategory("C4"));
        createCoupon("SAVE10", 10, 5);

        // 5 items = 50, minus a 10% coupon = 45
        JsonNode order = placeOrderWithCoupon(customer, product, 5, "SAVE10");
        assertEquals(45.0, order.get("total").asDouble());
    }

    @Test
    void couponUsageLimitIsEnforced() throws Exception {
        long customer = createCustomer("Finn");
        long product = createProduct("Fork", 10.0, 100, createCategory("C5"));
        createCoupon("ONCE", 10, 1);

        // first use is fine
        placeOrderWithCoupon(customer, product, 5, "ONCE");
        // second use is rejected (coupon used up)
        String body = orderBody(customer, product, 5, "ONCE");
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void payingAddsSurchargeAndLoyaltyAndAdvancesState() throws Exception {
        long customer = createCustomer("Gwen");
        long product = createProduct("Mug", 10.0, 100, createCategory("C6"));
        long orderId = placeOrder(customer, product, 5).get("id").asLong();

        JsonNode paid = action("/orders/" + orderId + "/pay");
        assertEquals("PAID", paid.get("status").asText());
        assertEquals(5.0, paid.get("surcharge").asDouble());
        assertEquals(50, getOk("/customers/" + customer).get("loyaltyPoints").asInt());
    }

    @Test
    void invoiceAddsTheSurchargeToTheTotal() throws Exception {
        long customer = createCustomer("Hank");
        long product = createProduct("Lamp", 10.0, 100, createCategory("C7"));
        long orderId = placeOrder(customer, product, 5).get("id").asLong();
        action("/orders/" + orderId + "/pay");

        JsonNode invoice = action("/orders/" + orderId + "/invoice");
        assertTrue(invoice.get("invoiceNumber").asLong() >= 1);
        assertEquals(50.0, invoice.get("total").asDouble());
        assertEquals(5.0, invoice.get("surcharge").asDouble());
        assertEquals(55.0, invoice.get("amountDue").asDouble());
    }

    @Test
    void unpaidOrderCannotBeInvoiced() throws Exception {
        long customer = createCustomer("Ivy");
        long product = createProduct("Vase", 10.0, 100, createCategory("C8"));
        long orderId = placeOrder(customer, product, 5).get("id").asLong();

        mockMvc.perform(post("/orders/" + orderId + "/invoice")).andExpect(status().isBadRequest());
    }

    @Test
    void cancellingRestocksTheItems() throws Exception {
        long customer = createCustomer("Jane");
        long product = createProduct("Tray", 10.0, 100, createCategory("C9"));
        long orderId = placeOrder(customer, product, 5).get("id").asLong();
        assertEquals(95, getOk("/products/" + product).get("stock").asInt());

        action("/orders/" + orderId + "/cancel");
        assertEquals(100, getOk("/products/" + product).get("stock").asInt());
    }

    @Test
    void orderBiggerThanStockIsRejected() throws Exception {
        long customer = createCustomer("Kyle");
        long product = createProduct("Pan", 10.0, 3, createCategory("C10"));

        String body = orderBody(customer, product, 10, null);
        mockMvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void auditAndMetricsRecordEvents() throws Exception {
        long customer = createCustomer("Lena");
        long product = createProduct("Bowl", 10.0, 100, createCategory("C11"));
        long orderId = placeOrder(customer, product, 5).get("id").asLong();
        action("/orders/" + orderId + "/pay");

        mockMvc.perform(get("/audit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("created order " + orderId)))
                .andExpect(content().string(containsString("paid order " + orderId)));

        JsonNode metrics = getOk("/metrics");
        assertTrue(metrics.get("ordersCreated").asInt() >= 1);
        assertTrue(metrics.get("ordersPaid").asInt() >= 1);
    }

    // ----- helpers --------------------------------------------------------

    private long createCategory(String name) throws Exception {
        return postOk("/categories", "{\"name\":\"" + name + "\"}").get("id").asLong();
    }

    private long createCustomer(String name) throws Exception {
        return postOk("/customers", "{\"name\":\"" + name + "\"}").get("id").asLong();
    }

    private long createProduct(String name, double price, int stock, long categoryId) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"price\":" + price
                + ",\"stock\":" + stock + ",\"categoryId\":" + categoryId + "}";
        return postOk("/products", body).get("id").asLong();
    }

    private void createCoupon(String code, int percent, int maxUses) throws Exception {
        String body = "{\"code\":\"" + code + "\",\"percent\":" + percent + ",\"maxUses\":" + maxUses + "}";
        postOk("/coupons", body);
    }

    private JsonNode placeOrder(long customerId, long productId, int quantity) throws Exception {
        return postOk("/orders", orderBody(customerId, productId, quantity, null));
    }

    private JsonNode placeOrderWithCoupon(long customerId, long productId, int quantity, String coupon)
            throws Exception {
        return postOk("/orders", orderBody(customerId, productId, quantity, coupon));
    }

    private String orderBody(long customerId, long productId, int quantity, String coupon) {
        String couponPart = (coupon == null) ? "" : "\"couponCode\":\"" + coupon + "\",";
        return "{" + couponPart + "\"customerId\":" + customerId
                + ",\"lines\":[{\"productId\":" + productId + ",\"quantity\":" + quantity + "}]}";
    }

    private JsonNode postOk(String url, String body) throws Exception {
        String response = mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }

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
