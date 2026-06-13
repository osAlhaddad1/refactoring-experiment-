package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ShopService {
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditPort auditPort;

    public ShopService(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       AuditPort auditPort) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditPort = auditPort;
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto dto) {
        Customer customer = new Customer(null, dto.getName(), 0);
        Customer saved = customerRepository.save(customer);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("customer not found"));
        return new CustomerDto(customer.getId(), customer.getName(), customer.getLoyaltyPoints());
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product(null, dto.getName(), dto.getPrice(), dto.getStock());
        Product saved = productRepository.save(product);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("product not found"));
        return new ProductDto(product.getId(), product.getName(), product.getPrice(), product.getStock());
    }

    @Transactional
    public OrderHeaderDto placeOrder(OrderHeaderDto dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("customer not found"));

        if (dto.getLines() == null || dto.getLines().isEmpty()) {
            throw new IllegalArgumentException("order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.setCustomerId(dto.getCustomerId());
        order.setStatus("NEW");

        double subtotal = 0;
        for (OrderLineDto lineDto : dto.getLines()) {
            Product product = productRepository.findById(lineDto.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("product not found"));

            if (lineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            if (product.getStock() < lineDto.getQuantity()) {
                throw new IllegalArgumentException("not enough stock");
            }

            double linePrice = product.getPrice() * lineDto.getQuantity();
            if (lineDto.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }
            
            OrderLine line = new OrderLine(null, lineDto.getProductId(), lineDto.getQuantity(), linePrice);
            order.getLines().add(line);
            subtotal += linePrice;

            product.setStock(product.getStock() - lineDto.getQuantity());
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        order.setTotal(total);

        OrderHeader saved = orderRepository.save(order);
        auditPort.log("created order " + saved.getId());

        return mapToOrderHeaderDto(saved);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("order not found"));
        return mapToOrderHeaderDto(order);
    }

    @Transactional
    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("order not found"));
        if (!"NEW".equals(order.getStatus())) {
            throw new IllegalArgumentException("only NEW orders can be paid");
        }
        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NoSuchElementException("customer not found"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        OrderHeader saved = orderRepository.save(order);
        auditPort.log("paid order " + saved.getId());
        return mapToOrderHeaderDto(saved);
    }

    @Transactional
    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("order not found"));
        if (!"PAID".equals(order.getStatus())) {
            throw new IllegalArgumentException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");
        OrderHeader saved = orderRepository.save(order);
        auditPort.log("shipped order " + saved.getId());
        return mapToOrderHeaderDto(saved);
    }

    @Transactional
    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("order not found"));
        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new IllegalArgumentException("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            });
        }

        order.setStatus("CANCELLED");
        OrderHeader saved = orderRepository.save(order);
        auditPort.log("cancelled order " + saved.getId());
        return mapToOrderHeaderDto(saved);
    }

    public List<String> getAuditLogs() {
        return auditPort.getLogs();
    }

    private OrderHeaderDto mapToOrderHeaderDto(OrderHeader order) {
        List<OrderLineDto> lineDtos = order.getLines().stream()
                .map(line -> new OrderLineDto(line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()))
                .collect(Collectors.toList());
        return new OrderHeaderDto(order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal(), lineDtos);
    }
}