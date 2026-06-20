package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
        customer.id = null;
        customer.name = dto.name;
        customer.loyaltyPoints = 0;
        
        Customer saved = customerRepository.save(customer);
        return mapToDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        return mapToDto(customer);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.id = null;
        product.name = dto.name;
        product.price = dto.price;
        product.stock = dto.stock;

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
        return mapToDto(product);
    }

    @Transactional
    public OrderHeaderDto placeOrder(OrderHeaderDto dto) {
        Customer customer = customerRepository.findById(dto.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        
        if (dto.lines == null || dto.lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.id = null;
        order.customerId = dto.customerId;
        order.status = "NEW";
        order.lines = new ArrayList<>();

        double subtotal = 0;
        for (OrderLineDto lineDto : dto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));
            
            if (lineDto.quantity <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity must be positive");
            }
            if (product.stock < lineDto.quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not enough stock");
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

            subtotal += linePrice;

            product.stock -= lineDto.quantity;
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        order.total = total;

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("created order " + saved.id);
        return mapToDto(saved);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        return mapToDto(order);
    }

    @Transactional
    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        
        if (!"NEW".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only NEW orders can be paid");
        }
        order.status = "PAID";

        Customer customer = customerRepository.findById(order.customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        customer.loyaltyPoints += (int) order.total;
        customerRepository.save(customer);

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("paid order " + saved.id);
        return mapToDto(saved);
    }

    @Transactional
    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        
        if (!"PAID".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only PAID orders can be shipped");
        }
        order.status = "SHIPPED";

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("shipped order " + saved.id);
        return mapToDto(saved);
    }

    @Transactional
    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        
        if ("SHIPPED".equals(order.status) || "CANCELLED".equals(order.status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot cancel a " + order.status + " order");
        }

        for (OrderLine line : order.lines) {
            productRepository.findById(line.productId).ifPresent(product -> {
                product.stock += line.quantity;
                productRepository.save(product);
            });
        }
        order.status = "CANCELLED";

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("cancelled order " + saved.id);
        return mapToDto(saved);
    }

    public List<String> getAuditLogs() {
        return auditLog.getLogs();
    }

    private CustomerDto mapToDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.id = customer.id;
        dto.name = customer.name;
        dto.loyaltyPoints = customer.loyaltyPoints;
        return dto;
    }

    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.id = product.id;
        dto.name = product.name;
        dto.price = product.price;
        dto.stock = product.stock;
        return dto;
    }

    private OrderHeaderDto mapToDto(OrderHeader order) {
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
}
