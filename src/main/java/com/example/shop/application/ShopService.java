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

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AuditLogPort auditLogPort;

    public ShopService(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       OrderRepository orderRepository,
                       AuditLogPort auditLogPort) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.auditLogPort = auditLogPort;
    }

    @Transactional
    public CustomerDto createCustomer(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setId(null);
        customer.setName(customerDto.name);
        customer.setLoyaltyPoints(0);

        Customer saved = customerRepository.save(customer);
        return toDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> notFound("customer"));
        return toDto(customer);
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setId(null);
        product.setName(productDto.name);
        product.setPrice(productDto.price);
        product.setStock(productDto.stock);

        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> notFound("product"));
        return toDto(product);
    }

    @Transactional
    public OrderDto placeOrder(OrderDto orderDto) {
        if (orderDto.customerId == null) {
            throw notFound("customer");
        }
        Customer customer = customerRepository.findById(orderDto.customerId)
                .orElseThrow(() -> notFound("customer"));

        if (orderDto.lines == null || orderDto.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        Order order = new Order();
        order.setId(null);
        order.setCustomerId(orderDto.customerId);

        double subtotal = 0;
        for (OrderLineDto lineDto : orderDto.lines) {
            Product product = productRepository.findById(lineDto.productId)
                    .orElseThrow(() -> notFound("product"));

            if (lineDto.quantity <= 0) {
                throw badRequest("quantity must be positive");
            }
            if (product.getStock() < lineDto.quantity) {
                throw badRequest("not enough stock");
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

            subtotal = subtotal + linePrice;

            product.setStock(product.getStock() - lineDto.quantity);
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }

        order.setStatus("NEW");
        order.setTotal(total);

        Order saved = orderRepository.save(order);
        auditLogPort.log("created order " + saved.getId());
        return toDto(saved);
    }

    public OrderDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        return toDto(order);
    }

    @Transactional
    public OrderDto payOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if (!order.getStatus().equals("NEW")) {
            throw badRequest("only NEW orders can be paid");
        }
        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> notFound("customer"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        Order saved = orderRepository.save(order);
        auditLogPort.log("paid order " + saved.getId());
        return toDto(saved);
    }

    @Transactional
    public OrderDto shipOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if (!order.getStatus().equals("PAID")) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");

        Order saved = orderRepository.save(order);
        auditLogPort.log("shipped order " + saved.getId());
        return toDto(saved);
    }

    @Transactional
    public OrderDto cancelOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if (order.getStatus().equals("SHIPPED") || order.getStatus().equals("CANCELLED")) {
            throw badRequest("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            }
        }
        order.setStatus("CANCELLED");

        Order saved = orderRepository.save(order);
        auditLogPort.log("cancelled order " + saved.getId());
        return toDto(saved);
    }

    public List<String> getAuditLogs() {
        return auditLogPort.getLogs();
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

    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.id = order.getId();
        dto.customerId = order.getCustomerId();
        dto.status = order.getStatus();
        dto.total = order.getTotal();
        dto.lines = order.getLines().stream().map(line -> {
            OrderLineDto ld = new OrderLineDto();
            ld.id = line.getId();
            ld.productId = line.getProductId();
            ld.quantity = line.getQuantity();
            ld.linePrice = line.getLinePrice();
            return ld;
        }).collect(Collectors.toList());
        return dto;
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
