package com.example.shop.presentation;

import com.example.shop.application.ShopApplicationService;
import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {
    private final ShopApplicationService shopService;

    public ShopController(ShopApplicationService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/customers")
    @Transactional
    public CustomerDto createCustomer(@RequestBody CustomerDto dto) {
        Customer customer = new Customer(null, dto.name, 0);
        Customer saved = shopService.createCustomer(customer);
        return mapToDto(saved);
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        try {
            Customer customer = shopService.getCustomer(id);
            return mapToDto(customer);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/products")
    @Transactional
    public ProductDto createProduct(@RequestBody ProductDto dto) {
        Product product = new Product(null, dto.name, dto.price, dto.stock);
        Product saved = shopService.createProduct(product);
        return mapToDto(saved);
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        try {
            Product product = shopService.getProduct(id);
            return mapToDto(product);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/orders")
    @Transactional
    public OrderDto placeOrder(@RequestBody OrderDto dto) {
        try {
            List<OrderLine> lines = dto.lines == null ? null : dto.lines.stream()
                    .map(l -> new OrderLine(null, l.productId, l.quantity, 0))
                    .collect(Collectors.toList());
            Order order = new Order(null, dto.customerId, null, 0, lines);
            Order saved = shopService.placeOrder(order);
            return mapToDto(saved);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        try {
            Order order = shopService.getOrder(id);
            return mapToDto(order);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/pay")
    @Transactional
    public OrderDto payOrder(@PathVariable Long id) {
        try {
            Order order = shopService.payOrder(id);
            return mapToDto(order);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/ship")
    @Transactional
    public OrderDto shipOrder(@PathVariable Long id) {
        try {
            Order order = shopService.shipOrder(id);
            return mapToDto(order);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/cancel")
    @Transactional
    public OrderDto cancelOrder(@PathVariable Long id) {
        try {
            Order order = shopService.cancelOrder(id);
            return mapToDto(order);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (InvalidOrderException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/audit")
    public List<String> audit() { 
        return shopService.getAuditLogs();
    }

    private CustomerDto mapToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.id = customer.getId();
        dto.name = customer.getName();
        dto.loyaltyPoints = customer.getLoyaltyPoints();
        return dto;
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.stock = product.getStock();
        return dto;
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.id = order.getId();
        dto.customerId = order.getCustomerId();
        dto.status = order.getStatus();
        dto.total = order.getTotal();
        dto.lines = order.getLines().stream().map(this::mapToDto).collect(Collectors.toList());
        return dto;
    }

    private OrderLineDto mapToDto(OrderLine line) {
        OrderLineDto dto = new OrderLineDto();
        dto.id = line.getId();
        dto.productId = line.getProductId();
        dto.quantity = line.getQuantity();
        dto.linePrice = line.getLinePrice();
        return dto;
    }
}
