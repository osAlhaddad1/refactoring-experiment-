package com.example.shop.presentation;

import com.example.shop.application.ShopService;
import com.example.shop.application.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/categories")
    public CategoryResponse createCategory(@RequestBody CategoryRequest request) {
        CategoryDto dto = new CategoryDto(null, request.getName());
        CategoryDto saved = shopService.createCategory(dto);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        CategoryDto saved = shopService.getCategory(id);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    @PostMapping("/customers")
    public CustomerResponse createCustomer(@RequestBody CustomerRequest request) {
        CustomerDto dto = new CustomerDto(null, request.getName(), 0);
        CustomerDto saved = shopService.createCustomer(dto);
        return new CustomerResponse(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @GetMapping("/customers/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        CustomerDto saved = shopService.getCustomer(id);
        return new CustomerResponse(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @PostMapping("/coupons")
    public CouponResponse createCoupon(@RequestBody CouponRequest request) {
        CouponDto dto = new CouponDto(request.getCode(), request.getPercent(), request.getMaxUses(), 0);
        CouponDto saved = shopService.createCoupon(dto);
        return new CouponResponse(saved.getCode(), saved.getPercent(), saved.getMaxUses(), saved.getTimesUsed());
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody ProductRequest request) {
        ProductDto saved = shopService.createProduct(request.getName(), request.getPrice(), request.getStock(), request.getCategoryId());
        CategoryResponse catResp = null;
        if (saved.getCategory() != null) {
            catResp = new CategoryResponse(saved.getCategory().getId(), saved.getCategory().getName());
        }
        return new ProductResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), catResp);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductDto saved = shopService.getProduct(id);
        CategoryResponse catResp = null;
        if (saved.getCategory() != null) {
            catResp = new CategoryResponse(saved.getCategory().getId(), saved.getCategory().getName());
        }
        return new ProductResponse(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock(), catResp);
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        List<OrderLineDto> lineDtos = List.of();
        if (request.getLines() != null) {
            lineDtos = request.getLines().stream()
                    .map(line -> new OrderLineDto(null, line.getProductId(), line.getQuantity(), 0.0))
                    .collect(Collectors.toList());
        }
        OrderDto dto = new OrderDto(null, request.getCustomerId(), null, 0.0, 0.0, request.getCouponCode(), lineDtos);
        OrderDto saved = shopService.placeOrder(dto);
        return mapOrder(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderDto saved = shopService.getOrder(id);
        return mapOrder(saved);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderResponse payOrder(@PathVariable Long id) {
        OrderDto saved = shopService.payOrder(id);
        return mapOrder(saved);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        OrderDto saved = shopService.shipOrder(id);
        return mapOrder(saved);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        OrderDto saved = shopService.cancelOrder(id);
        return mapOrder(saved);
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return shopService.invoiceOrder(id);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }

    private OrderResponse mapOrder(OrderDto order) {
        List<OrderLineResponse> lines = order.getLines().stream()
                .map(line -> new OrderLineResponse(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), order.getSurcharge(), order.getCouponCode(), lines);
    }
}
