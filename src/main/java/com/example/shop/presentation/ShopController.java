package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto category) {
        return shopService.createCategory(category);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return shopService.getCategory(id);
    }

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customer) {
        return shopService.createCustomer(customer);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        return shopService.getCustomer(id);
    }

    @PostMapping("/coupons")
    public CouponDto createCoupon(@RequestBody CouponDto coupon) {
        return shopService.createCoupon(coupon);
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody Map<String, Object> body) {
        ProductCreateDto dto = new ProductCreateDto();
        dto.name = (String) body.get("name");
        dto.price = ((Number) body.get("price")).doubleValue();
        dto.stock = ((Number) body.get("stock")).intValue();
        Object categoryId = body.get("categoryId");
        if (categoryId != null) {
            dto.categoryId = ((Number) categoryId).longValue();
        }
        return shopService.createProduct(dto);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return shopService.getProduct(id);
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        return shopService.placeOrder(order);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return shopService.getOrder(id);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        return shopService.payOrder(id);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        return shopService.shipOrder(id);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        return shopService.cancelOrder(id);
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return shopService.invoiceOrder(id);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAudit();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }
}
