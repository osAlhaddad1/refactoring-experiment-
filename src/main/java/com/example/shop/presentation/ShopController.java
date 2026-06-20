package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final CustomerService customerService;
    private final ProductService productService;
    private final OrderService orderService;
    private final AuditService auditService;

    public ShopController(CustomerService customerService,
                          ProductService productService,
                          OrderService orderService,
                          AuditService auditService) {
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
        this.auditService = auditService;
    }

    // ----- customers ------------------------------------------------------

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customerDto) {
        com.example.shop.application.CustomerDto appDto = new com.example.shop.application.CustomerDto();
        appDto.setName(customerDto.name);
        com.example.shop.application.CustomerDto saved = customerService.createCustomer(appDto);
        return mapToPresentation(saved);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        com.example.shop.application.CustomerDto saved = customerService.getCustomer(id);
        return mapToPresentation(saved);
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        com.example.shop.application.ProductDto appDto = new com.example.shop.application.ProductDto();
        appDto.setName(productDto.name);
        appDto.setPrice(productDto.price);
        appDto.setStock(productDto.stock);
        com.example.shop.application.ProductDto saved = productService.createProduct(appDto);
        return mapToPresentation(saved);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        com.example.shop.application.ProductDto saved = productService.getProduct(id);
        return mapToPresentation(saved);
    }

    // ----- orders ---------------------------------------------------------

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto orderDto) {
        com.example.shop.application.OrderDto appDto = new com.example.shop.application.OrderDto();
        appDto.setCustomerId(orderDto.customerId);
        if (orderDto.lines != null) {
            appDto.setLines(orderDto.lines.stream().map(line -> {
                com.example.shop.application.OrderLineDto l = new com.example.shop.application.OrderLineDto();
                l.setProductId(line.productId);
                l.setQuantity(line.quantity);
                return l;
            }).collect(Collectors.toList()));
        }
        com.example.shop.application.OrderDto saved = orderService.placeOrder(appDto);
        return mapToPresentation(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        com.example.shop.application.OrderDto saved = orderService.getOrder(id);
        return mapToPresentation(saved);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        com.example.shop.application.OrderDto saved = orderService.payOrder(id);
        return mapToPresentation(saved);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        com.example.shop.application.OrderDto saved = orderService.shipOrder(id);
        return mapToPresentation(saved);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        com.example.shop.application.OrderDto saved = orderService.cancelOrder(id);
        return mapToPresentation(saved);
    }

    // ----- audit ----------------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return auditService.getAuditLogs();
    }

    // ----- exception handlers ---------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleNotFound(ResourceNotFoundException ex) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
    }

    @ExceptionHandler(ValidationException.class)
    public void handleBadRequest(ValidationException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }

    // ----- mappers --------------------------------------------------------

    private CustomerDto mapToPresentation(com.example.shop.application.CustomerDto appDto) {
        CustomerDto dto = new CustomerDto();
        dto.id = appDto.getId();
        dto.name = appDto.getName();
        dto.loyaltyPoints = appDto.getLoyaltyPoints();
        return dto;
    }

    private ProductDto mapToPresentation(com.example.shop.application.ProductDto appDto) {
        ProductDto dto = new ProductDto();
        dto.id = appDto.getId();
        dto.name = appDto.getName();
        dto.price = appDto.getPrice();
        dto.stock = appDto.getStock();
        return dto;
    }

    private OrderDto mapToPresentation(com.example.shop.application.OrderDto appDto) {
        OrderDto dto = new OrderDto();
        dto.id = appDto.getId();
        dto.customerId = appDto.getCustomerId();
        dto.status = appDto.getStatus();
        dto.total = appDto.getTotal();
        if (appDto.getLines() != null) {
            dto.lines = appDto.getLines().stream().map(line -> {
                OrderLineDto l = new OrderLineDto();
                l.id = line.getId();
                l.productId = line.getProductId();
                l.quantity = line.getQuantity();
                l.linePrice = line.getLinePrice();
                return l;
            }).collect(Collectors.toList());
        }
        return dto;
    }
}
