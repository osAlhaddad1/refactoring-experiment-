package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final CustomerRepositoryPort customerRepository;
    private final ProductRepositoryPort productRepository;
    private final OrderRepositoryPort orderRepository;
    private final AuditLogPort auditLog;

    public ShopService(CustomerRepositoryPort customerRepository,
                       ProductRepositoryPort productRepository,
                       OrderRepositoryPort orderRepository,
                       AuditLogPort auditLog) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditLog = auditLog;
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer();
        customer.name = dto.name;
        customer.loyaltyPoints = 0;
        
        Customer saved = customerRepository.save(customer);
        return toDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> notFound("customer"));
        return toDto(customer);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.name = dto.name;
        product.price = dto.price;
        product.stock = dto.stock;
        
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> notFound("product"));
        return toDto(product);
    }

    @Transactional
    public OrderHeaderDto placeOrder(OrderHeaderDto dto) {
        Customer customer = customerRepository.findById(dto.customerId)
                .orElseThrow(() -> notFound("customer"));
        
        if (dto.lines == null || dto.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.customerId = dto.customerId;
        
        double subtotal = 0;
        for (OrderLineDto lineDto : dto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> notFound("product"));
            
            if (lineDto.quantity <= 0) {
                throw badRequest("quantity must be positive");
            }
            if (product.stock < lineDto.quantity) {
                throw badRequest("not enough stock");
            }

            double linePrice = product.price * lineDto.quantity;
            if (lineDto.quantity >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            
            OrderLine line = new OrderLine();
            line.productId = lineDto.productId;
            line.quantity = lineDto.quantity;
            line.linePrice = linePrice;
            order.lines.add(line);
            
            subtotal = subtotal + linePrice;

            product.stock = product.stock - lineDto.quantity;
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        order.status = "NEW";
        order.total = total;
        
        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.log("created order " + savedOrder.id);
        return toDto(savedOrder);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        return toDto(order);
    }

    @Transactional
    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (!order.status.equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.status = "PAID";
        
        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> notFound("customer"));
        customer.loyaltyPoints = customer.loyaltyPoints + (int) order.total;
        customerRepository.save(customer);
        
        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.log("paid order " + savedOrder.id);
        return toDto(savedOrder);
    }

    @Transactional
    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (!order.status.equals("PAID")) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.status = "SHIPPED";
        
        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.log("shipped order " + savedOrder.id);
        return toDto(savedOrder);
    }

    @Transactional
    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        if (order.status.equals("SHIPPED") || order.status.equals("CANCELLED")) {
            throw badRequest("cannot cancel a " + order.status + " order");
        }
        
        for (OrderLine line : order.lines) {
            productRepository.findById(line.productId).ifPresent(product -> {
                product.stock = product.stock + line.quantity;
                productRepository.save(product);
            });
        }
        order.status = "CANCELLED";
        
        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.log("cancelled order " + savedOrder.id);
        return toDto(savedOrder);
    }

    public List<String> getAuditLogs() {
        return auditLog.getLogs();
    }

    private CustomerDto toDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        return dto;
    }

    private OrderHeaderDto toDto(OrderHeader order) {
        OrderHeaderDto dto = new OrderHeaderDto();
        dto.id = order.id;
        dto.customerId = order.customerId;
        dto.status = order.status;
        dto.total = order.total;
        if (order.lines != null) {
            dto.lines = order.lines.stream().map(line -> {
                OrderLineDto ld = new OrderLineDto();
                ld.id = line.id;
                ld.productId = line.productId;
                ld.quantity = line.quantity;
                ld.linePrice = line.linePrice;
                return ld;
            }).collect(Collectors.toList());
        }
        return dto;
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}