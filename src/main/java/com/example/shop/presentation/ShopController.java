package com.example.shop.presentation;

import com.example.shop.application.ShopApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ShopController {

    private final ShopApplicationService shopApplicationService;

    public ShopController(ShopApplicationService shopApplicationService) {
        this.shopApplicationService = shopApplicationService;
    }

    // ----- categories -----

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto categoryDto) {
        Map<String, Object> result = shopApplicationService.createCategory(categoryDto.name);
        return new CategoryDto(result);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.getCategory(id);
        return new CategoryDto(result);
    }

    // ----- customers -----

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        Map<String, Object> result = shopApplicationService.createCustomer(customerDto.name);
        return new CustomerDto(result);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.getCustomer(id);
        return new CustomerDto(result);
    }

    // ----- coupons -----

    @PostMapping("/coupons")
    public CouponDto createCoupon(@RequestBody CouponDto couponDto) {
        Map<String, Object> result = shopApplicationService.createCoupon(couponDto.code, couponDto.percent, couponDto.maxUses);
        return new CouponDto(result);
    }

    // ----- products -----

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int stock = ((Number) body.get("stock")).intValue();
        Long categoryId = null;
        Object catIdObj = body.get("categoryId");
        if (catIdObj != null) {
            categoryId = ((Number) catIdObj).longValue();
        }
        Map<String, Object> result = shopApplicationService.createProduct(name, price, stock, categoryId);
        return new ProductDto(result);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.getProduct(id);
        return new ProductDto(result);
    }

    // ----- orders -----

    @PostMapping("/orders")
    public OrderHeaderDto placeOrder(@RequestBody OrderHeaderDto orderDto) {
        List<Map<String, Object>> linesData = new ArrayList<>();
        if (orderDto.lines != null) {
            for (OrderLineDto line : orderDto.lines) {
                Map<String, Object> lineMap = new LinkedHashMap<>();
                lineMap.put("productId", line.productId);
                lineMap.put("quantity", line.quantity);
                linesData.add(lineMap);
            }
        }
        Map<String, Object> result = shopApplicationService.placeOrder(orderDto.customerId, orderDto.couponCode, linesData);
        return new OrderHeaderDto(result);
    }

    @GetMapping("/orders/{id}")
    public OrderHeaderDto getOrder(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.getOrder(id);
        return new OrderHeaderDto(result);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeaderDto payOrder(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.payOrder(id);
        return new OrderHeaderDto(result);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeaderDto shipOrder(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.shipOrder(id);
        return new OrderHeaderDto(result);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeaderDto cancelOrder(@PathVariable Long id) {
        Map<String, Object> result = shopApplicationService.cancelOrder(id);
        return new OrderHeaderDto(result);
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return shopApplicationService.invoiceOrder(id);
    }

    // ----- audit + metrics -----

    @GetMapping("/audit")
    public List<String> audit() {
        return shopApplicationService.getAudit();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopApplicationService.getMetrics();
    }
}