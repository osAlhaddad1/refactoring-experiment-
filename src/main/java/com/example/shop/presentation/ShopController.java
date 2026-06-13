package com.example.shop.presentation;

import com.example.shop.application.*;
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
        CategoryDto input = new CategoryDto(null, request.name);
        CategoryDto output = shopService.createCategory(input);
        return new CategoryResponse(output.id, output.name);
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getCategory(@PathVariable Long id) {
        CategoryDto output = shopService.getCategory(id);
        return new CategoryResponse(output.id, output.name);
    }

    @PostMapping("/customers")
    public CustomerResponse createCustomer(@RequestBody CustomerRequest request) {
        CustomerDto input = new CustomerDto(null, request.name, 0);
        CustomerDto output = shopService.createCustomer(input);
        return new CustomerResponse(output.id, output.name, output.loyaltyPoints);
    }

    @GetMapping("/customers/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        CustomerDto output = shopService.getCustomer(id);
        return new CustomerResponse(output.id, output.name, output.loyaltyPoints);
    }

    @PostMapping("/coupons")
    public CouponResponse createCoupon(@RequestBody CouponRequest request) {
        CouponDto input = new CouponDto(request.code, request.percent, request.maxUses, 0);
        CouponDto output = shopService.createCoupon(input);
        return new CouponResponse(output.code, output.percent, output.maxUses, output.timesUsed);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int stock = ((Number) body.get("stock")).intValue();
        Object categoryIdObj = body.get("categoryId");
        Long categoryId = categoryIdObj != null ? ((Number) categoryIdObj).longValue() : null;

        ProductDto output = shopService.createProduct(name, price, stock, categoryId);
        CategoryResponse catResp = output.category != null ? new CategoryResponse(output.category.id, output.category.name) : null;
        return new ProductResponse(output.id, output.name, output.price, output.stock, catResp);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductDto output = shopService.getProduct(id);
        CategoryResponse catResp = output.category != null ? new CategoryResponse(output.category.id, output.category.name) : null;
        return new ProductResponse(output.id, output.name, output.price, output.stock, catResp);
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody OrderRequest request) {
        List<OrderLineDto> lineDtos = request.lines != null ? request.lines.stream()
                .map(l -> new OrderLineDto(null, l.productId, l.quantity, 0))
                .collect(Collectors.toList()) : null;

        OrderHeaderDto input = new OrderHeaderDto(
                null,
                request.customerId,
                null,
                0,
                0,
                request.couponCode,
                lineDtos
        );

        OrderHeaderDto output = shopService.placeOrder(input);
        return mapToOrderResponse(output);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderHeaderDto output = shopService.getOrder(id);
        return mapToOrderResponse(output);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderResponse payOrder(@PathVariable Long id) {
        OrderHeaderDto output = shopService.payOrder(id);
        return mapToOrderResponse(output);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        OrderHeaderDto output = shopService.shipOrder(id);
        return mapToOrderResponse(output);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        OrderHeaderDto output = shopService.cancelOrder(id);
        return mapToOrderResponse(output);
    }

    @PostMapping("/orders/{id}/invoice")
    public InvoiceResponse invoiceOrder(@PathVariable Long id) {
        InvoiceDto output = shopService.invoiceOrder(id);
        return new InvoiceResponse(output.invoiceNumber, output.orderId, output.total, output.surcharge, output.amountDue);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }

    private OrderResponse mapToOrderResponse(OrderHeaderDto dto) {
        List<OrderLineResponse> lineResponses = dto.lines.stream()
                .map(l -> new OrderLineResponse(l.id, l.productId, l.quantity, l.linePrice))
                .collect(Collectors.toList());
        return new OrderResponse(
                dto.id,
                dto.customerId,
                dto.status,
                dto.total,
                dto.surcharge,
                dto.couponCode,
                lineResponses
        );
    }
}
