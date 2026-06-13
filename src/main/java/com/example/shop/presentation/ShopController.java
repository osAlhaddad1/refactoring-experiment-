package com.example.shop.presentation;

import com.example.shop.application.ShopService;
import com.example.shop.domain.*;
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

    // ----- categories -----------------------------------------------------

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto dto) {
        Category category = new Category(dto.id, dto.name);
        Category saved = shopService.createCategory(category);
        return mapToDto(saved);
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        Category category = shopService.getCategory(id);
        return mapToDto(category);
    }

    // ----- customers ------------------------------------------------------

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto dto) {
        Customer customer = new Customer(dto.id, dto.name, dto.loyaltyPoints);
        Customer saved = shopService.createCustomer(customer);
        return mapToDto(saved);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        Customer customer = shopService.getCustomer(id);
        return mapToDto(customer);
    }

    // ----- coupons --------------------------------------------------------

    @PostMapping("/coupons")
    public CouponDto createCoupon(@RequestBody CouponDto dto) {
        Coupon coupon = new Coupon(dto.code, dto.percent, dto.maxUses, dto.timesUsed);
        Coupon saved = shopService.createCoupon(coupon);
        return mapToDto(saved);
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        double price = ((Number) body.get("price")).doubleValue();
        int stock = ((Number) body.get("stock")).intValue();
        Object categoryIdObj = body.get("categoryId");
        Long categoryId = categoryIdObj != null ? ((Number) categoryIdObj).longValue() : null;

        Product saved = shopService.createProduct(name, price, stock, categoryId);
        return mapToDto(saved);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        Product product = shopService.getProduct(id);
        return mapToDto(product);
    }

    // ----- orders ---------------------------------------------------------

    @PostMapping("/orders")
    public OrderHeaderDto placeOrder(@RequestBody OrderHeaderDto dto) {
        OrderHeader order = mapToDomain(dto);
        OrderHeader saved = shopService.placeOrder(order);
        return mapToDto(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderHeaderDto getOrder(@PathVariable Long id) {
        OrderHeader order = shopService.getOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeaderDto payOrder(@PathVariable Long id) {
        OrderHeader order = shopService.payOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeaderDto shipOrder(@PathVariable Long id) {
        OrderHeader order = shopService.shipOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeaderDto cancelOrder(@PathVariable Long id) {
        OrderHeader order = shopService.cancelOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        return shopService.invoiceOrder(id);
    }

    // ----- audit + metrics ------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLog();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }

    // ----- mappers --------------------------------------------------------

    private CategoryDto mapToDto(Category category) {
        if (category == null) return null;
        CategoryDto dto = new CategoryDto();
        dto.id = category.getId();
        dto.name = category.getName();
        return dto;
    }

    private CustomerDto mapToDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.id = customer.getId();
        dto.name = customer.getName();
        dto.loyaltyPoints = customer.getLoyaltyPoints();
        return dto;
    }

    private CouponDto mapToDto(Coupon coupon) {
        if (coupon == null) return null;
        CouponDto dto = new CouponDto();
        dto.code = coupon.getCode();
        dto.percent = coupon.getPercent();
        dto.maxUses = coupon.getMaxUses();
        dto.timesUsed = coupon.getTimesUsed();
        return dto;
    }

    private ProductDto mapToDto(Product product) {
        if (product == null) return null;
        ProductDto dto = new ProductDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.stock = product.getStock();
        dto.category = mapToDto(product.getCategory());
        return dto;
    }

    private OrderHeaderDto mapToDto(OrderHeader order) {
        if (order == null) return null;
        OrderHeaderDto dto = new OrderHeaderDto();
        dto.id = order.getId();
        dto.customerId = order.getCustomerId();
        dto.status = order.getStatus();
        dto.total = order.getTotal();
        dto.surcharge = order.getSurcharge();
        dto.couponCode = order.getCouponCode();
        if (order.getLines() != null) {
            dto.lines = order.getLines().stream()
                    .map(line -> {
                        OrderLineDto ld = new OrderLineDto();
                        ld.id = line.getId();
                        ld.productId = line.getProductId();
                        ld.quantity = line.getQuantity();
                        ld.linePrice = line.getLinePrice();
                        return ld;
                    }).collect(Collectors.toList());
        }
        return dto;
    }

    private OrderHeader mapToDomain(OrderHeaderDto dto) {
        if (dto == null) return null;
        List<OrderLine> lines = null;
        if (dto.lines != null) {
            lines = dto.lines.stream()
                    .map(ld -> new OrderLine(ld.id, ld.productId, ld.quantity, ld.linePrice))
                    .collect(Collectors.toList());
        }
        return new OrderHeader(
                dto.id,
                dto.customerId,
                dto.status,
                dto.total,
                dto.surcharge,
                dto.couponCode,
                lines
        );
    }
}