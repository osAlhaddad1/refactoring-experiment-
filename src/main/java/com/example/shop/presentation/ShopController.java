package com.example.shop.presentation;

import com.example.shop.application.ShopService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto categoryDto) {
        com.example.shop.application.CategoryDto appDto = new com.example.shop.application.CategoryDto();
        appDto.name = categoryDto.name;
        
        com.example.shop.application.CategoryDto saved = shopService.createCategory(appDto);
        return mapCategory(saved);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        return mapCategory(shopService.getCategory(id));
    }

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        com.example.shop.application.CustomerDto appDto = new com.example.shop.application.CustomerDto();
        appDto.name = customerDto.name;
        
        com.example.shop.application.CustomerDto saved = shopService.createCustomer(appDto);
        return mapCustomer(saved);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        return mapCustomer(shopService.getCustomer(id));
    }

    @PostMapping("/coupons")
    public CouponDto createCoupon(@RequestBody CouponDto couponDto) {
        com.example.shop.application.CouponDto appDto = new com.example.shop.application.CouponDto();
        appDto.code = couponDto.code;
        appDto.percent = couponDto.percent;
        appDto.maxUses = couponDto.maxUses;
        
        com.example.shop.application.CouponDto saved = shopService.createCoupon(appDto);
        return mapCoupon(saved);
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int stock = ((Number) body.get("stock")).intValue();
        Object categoryIdObj = body.get("categoryId");
        Long categoryId = categoryIdObj != null ? ((Number) categoryIdObj).longValue() : null;

        com.example.shop.application.ProductDto saved = shopService.createProduct(name, price, stock, categoryId);
        return mapProduct(saved);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        return mapProduct(shopService.getProduct(id));
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto orderDto) {
        com.example.shop.application.OrderDto appDto = new com.example.shop.application.OrderDto();
        appDto.customerId = orderDto.customerId;
        appDto.couponCode = orderDto.couponCode;
        appDto.lines = new ArrayList<>();
        if (orderDto.lines != null) {
            for (OrderLineDto line : orderDto.lines) {
                com.example.shop.application.OrderLineDto appLine = new com.example.shop.application.OrderLineDto();
                appLine.productId = line.productId;
                appLine.quantity = line.quantity;
                appDto.lines.add(appLine);
            }
        }

        com.example.shop.application.OrderDto saved = shopService.placeOrder(appDto);
        return mapOrder(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return mapOrder(shopService.getOrder(id));
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        return mapOrder(shopService.payOrder(id));
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        return mapOrder(shopService.shipOrder(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        return mapOrder(shopService.cancelOrder(id));
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

    private CategoryDto mapCategory(com.example.shop.application.CategoryDto appDto) {
        if (appDto == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = appDto.id;
        dto.name = appDto.name;
        return dto;
    }

    private CustomerDto mapCustomer(com.example.shop.application.CustomerDto appDto) {
        if (appDto == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = appDto.id;
        dto.name = appDto.name;
        dto.loyaltyPoints = appDto.loyaltyPoints;
        return dto;
    }

    private CouponDto mapCoupon(com.example.shop.application.CouponDto appDto) {
        if (appDto == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = appDto.code;
        dto.percent = appDto.percent;
        dto.maxUses = appDto.maxUses;
        dto.timesUsed = appDto.timesUsed;
        return dto;
    }

    private ProductDto mapProduct(com.example.shop.application.ProductDto appDto) {
        if (appDto == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = appDto.id;
        dto.name = appDto.name;
        dto.price = appDto.price;
        dto.stock = appDto.stock;
        dto.category = mapCategory(appDto.category);
        return dto;
    }

    private OrderDto mapOrder(com.example.shop.application.OrderDto appDto) {
        if (appDto == null) return null;
        OrderDto dto = new OrderDto();
        dto.id = appDto.id;
        dto.customerId = appDto.customerId;
        dto.status = appDto.status;
        dto.total = appDto.total;
        dto.surcharge = appDto.surcharge;
        dto.couponCode = appDto.couponCode;
        dto.lines = new ArrayList<>();
        if (appDto.lines != null) {
            for (com.example.shop.application.OrderLineDto appLine : appDto.lines) {
                OrderLineDto lineDto = new OrderLineDto();
                lineDto.id = appLine.id;
                lineDto.productId = appLine.productId;
                lineDto.quantity = appLine.quantity;
                lineDto.linePrice = appLine.linePrice;
                dto.lines.add(lineDto);
            }
        }
        return dto;
    }
}