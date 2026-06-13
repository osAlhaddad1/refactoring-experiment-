package com.example.shop.presentation;

import com.example.shop.application.AuditApplicationService;
import com.example.shop.application.CustomerApplicationService;
import com.example.shop.application.OrderApplicationService;
import com.example.shop.application.ProductApplicationService;
import com.example.shop.domain.Customer;
import com.example.shop.domain.OrderHeader;
import com.example.shop.domain.OrderLine;
import com.example.shop.domain.Product;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final CustomerApplicationService customerService;
    private final ProductApplicationService productService;
    private final OrderApplicationService orderService;
    private final AuditApplicationService auditService;

    public ShopController(CustomerApplicationService customerService,
                          ProductApplicationService productService,
                          OrderApplicationService orderService,
                          AuditApplicationService auditService) {
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
        this.auditService = auditService;
    }

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto dto) {
        Customer customer = new Customer(null, dto.name, 0);
        Customer saved = customerService.createCustomer(customer);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        Customer customer = customerService.getCustomer(id);
        return new CustomerDto(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        Product product = new Product(null, dto.name, dto.price, dto.stock);
        Product saved = productService.createProduct(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        Product product = productService.getProduct(id);
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    @PostMapping("/orders")
    public OrderHeaderDto placeOrder(@RequestBody OrderHeaderDto dto) {
        List<OrderLine> lines = dto.lines == null ? null : dto.lines.stream()
                .map(lineDto -> new OrderLine(null, lineDto.productId, lineDto.quantity, 0))
                .collect(Collectors.toList());

        OrderHeader order = orderService.placeOrder(dto.customerId, lines);
        return mapToDto(order);
    }

    @GetMapping("/orders/{id}")
    public OrderHeaderDto getOrder(@PathVariable Long id) {
        OrderHeader order = orderService.getOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeaderDto payOrder(@PathVariable Long id) {
        OrderHeader order = orderService.payOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeaderDto shipOrder(@PathVariable Long id) {
        OrderHeader order = orderService.shipOrder(id);
        return mapToDto(order);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeaderDto cancelOrder(@PathVariable Long id) {
        OrderHeader order = orderService.cancelOrder(id);
        return mapToDto(order);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return auditService.getAuditLogs();
    }

    private OrderHeaderDto mapToDto(OrderHeader order) {
        List<OrderLineDto> lineDtos = order.getLines().stream()
                .map(line -> new OrderLineDto(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderHeaderDto(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), lineDtos);
    }
}