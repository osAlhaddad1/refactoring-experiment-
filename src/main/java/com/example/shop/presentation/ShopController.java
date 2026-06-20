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
    public CustomerDto createCustomer(@RequestBody CustomerDto dto) {
        try {
            CustomerInfo info = customerService.createCustomer(dto.name);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        try {
            CustomerInfo info = customerService.getCustomer(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ----- products -------------------------------------------------------

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        try {
            ProductInfo info = productService.createProduct(dto.name, dto.price, dto.stock);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        try {
            ProductInfo info = productService.getProduct(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ----- orders ---------------------------------------------------------

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto dto) {
        try {
            List<OrderLineInfo> lineInfos = dto.lines == null ? null : dto.lines.stream()
                    .map(line -> new OrderLineInfo(null, line.productId, line.quantity, 0))
                    .collect(Collectors.toList());
            OrderInfo info = orderService.placeOrder(dto.customerId, lineInfos);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        try {
            OrderInfo info = orderService.getOrder(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        try {
            OrderInfo info = orderService.payOrder(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        try {
            OrderInfo info = orderService.shipOrder(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        try {
            OrderInfo info = orderService.cancelOrder(id);
            return toDto(info);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage() + " not found");
        } catch (BusinessRuleException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ----- audit ----------------------------------------------------------

    @GetMapping("/audit")
    public List<String> audit() {
        return auditService.getAuditLogs();
    }

    // ----- mappers --------------------------------------------------------

    private CustomerDto toDto(CustomerInfo info) {
        return new CustomerDto(info.getId(), info.getName(), info.getLoyaltyPoints());
    }

    private ProductDto toDto(ProductInfo info) {
        return new ProductDto(info.getId(), info.getName(), info.getPrice(), info.getStock());
    }

    private OrderDto toDto(OrderInfo info) {
        List<OrderLineDto> lines = info.getLines().stream()
                .map(line -> new OrderLineDto(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderDto(info.getId(), info.getCustomerId(), info.getStatus(), info.getTotal(), lines);
    }
}