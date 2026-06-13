package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditLog auditLog;

    public ShopService(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       AuditLog auditLog) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditLog = auditLog;
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        customer.setName(dto.name);
        customer.setLoyaltyPoints(0);
        customer = customerRepository.save(customer);
        return toDto(customer);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        return toDto(customer);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setName(dto.name);
        product.setPrice(dto.price);
        product.setStock(dto.stock);
        product = productRepository.save(product);
        return toDto(product);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return toDto(product);
    }

    @Transactional
    public OrderDto placeOrder(OrderDto dto) {
        Customer customer = customerRepository.findById(dto.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        if (dto.lines == null || dto.lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.setCustomerId(dto.customerId);
        order.setStatus("NEW");

        double subtotal = 0;
        for (OrderLineDto lineDto : dto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
            if (lineDto.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
            }
            if (product.getStock() < lineDto.quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
            }

            double linePrice = product.getPrice() * lineDto.quantity;
            if (lineDto.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            
            OrderLine line = new OrderLine();
            line.setProductId(lineDto.productId);
            line.setQuantity(lineDto.quantity);
            line.setLinePrice(linePrice);
            order.getLines().add(line);

            subtotal += linePrice;

            product.setStock(product.getStock() - lineDto.quantity);
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        order.setTotal(total);

        order = orderRepository.save(order);
        auditLog.log("created order " + order.getId());
        return toDto(order);
    }

    public OrderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return toDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if (!"NEW".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only NEW orders can be paid");
        }
        order.setStatus("PAID");
        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);
        order = orderRepository.save(order);
        auditLog.log("paid order " + order.getId());
        return toDto(order);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if (!"PAID".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");
        order = orderRepository.save(order);
        auditLog.log("shipped order " + order.getId());
        return toDto(order);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot cancel a " + order.getStatus() + " order");
        }
        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            });
        }
        order.setStatus("CANCELLED");
        order = orderRepository.save(order);
        auditLog.log("cancelled order " + order.getId());
        return toDto(order);
    }

    public List<String> getAuditLogs() {
        return auditLog.getLogs();
    }

    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.id = customer.getId();
        dto.name = customer.getName();
        dto.loyaltyPoints = customer.getLoyaltyPoints();
        return dto;
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.price = product.getPrice();
        dto.stock = product.getStock();
        return dto;
    }

    private OrderDto toDto(OrderHeader order) {
        OrderDto dto = new OrderDto();
        dto.id = order.getId();
        dto.customerId = order.getCustomerId();
        dto.status = order.getStatus();
        dto.total = order.getTotal();
        if (order.getLines() != null) {
            dto.lines = order.getLines().stream().map(line -> {
                OrderLineDto lineDto = new OrderLineDto();
                lineDto.id = line.getId();
                lineDto.productId = line.getProductId();
                lineDto.quantity = line.getQuantity();
                lineDto.linePrice = line.getLinePrice();
                return lineDto;
            }).collect(Collectors.toList());
        }
        return dto;
    }
}