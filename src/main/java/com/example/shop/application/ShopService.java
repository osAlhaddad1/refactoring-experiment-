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
        customer.setId(null);
        customer.setName(dto.name);
        customer.setLoyaltyPoints(0);

        Customer saved = customerRepository.save(customer);
        return mapToDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> notFound("customer"));
        return mapToDto(customer);
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product product = new Product();
        product.setId(null);
        product.setName(dto.name);
        product.setPrice(dto.price);
        product.setStock(dto.stock);

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> notFound("product"));
        return mapToDto(product);
    }

    @Transactional
    public OrderHeaderDto placeOrder(OrderHeaderDto dto) {
        Customer customer = customerRepository.findById(dto.customerId)
                .orElseThrow(() -> notFound("customer"));

        if (dto.lines == null || dto.lines.isEmpty()) {
            throw badRequest("order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.setId(null);
        order.setCustomerId(dto.customerId);
        order.setStatus("NEW");

        double subtotal = 0;
        List<OrderLine> lines = new ArrayList<>();
        for (OrderLineDto lineDto : dto.lines) {
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
            line.setId(null);
            line.setProductId(lineDto.productId);
            line.setQuantity(lineDto.quantity);
            line.setLinePrice(linePrice);
            lines.add(line);

            subtotal += linePrice;

            product.setStock(product.getStock() - lineDto.quantity);
            productRepository.save(product);
        }
        order.setLines(lines);

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        order.setTotal(total);

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("created order " + saved.getId());

        return mapToDto(saved);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));
        return mapToDto(order);
    }

    @Transactional
    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if (!"NEW".equals(order.getStatus())) {
            throw badRequest("only NEW orders can be paid");
        }
        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> notFound("customer"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("paid order " + saved.getId());

        return mapToDto(saved);
    }

    @Transactional
    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if (!"PAID".equals(order.getStatus())) {
            throw badRequest("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("shipped order " + saved.getId());

        return mapToDto(saved);
    }

    @Transactional
    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> notFound("order"));

        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw badRequest("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            productRepository.findById(line.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            });
        }
        order.setStatus("CANCELLED");

        OrderHeader saved = orderRepository.save(order);
        auditLog.log("cancelled order " + saved.getId());

        return mapToDto(saved);
    }

    public List<String> getAuditLogs() {
        return auditLog.getLogs();
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

    private OrderHeaderDto mapToDto(OrderHeader order) {
        OrderHeaderDto dto = new OrderHeaderDto();
        dto.id = order.getId();
        dto.customerId = order.getCustomerId();
        dto.status = order.getStatus();
        dto.total = order.getTotal();
        dto.lines = order.getLines().stream().map(line -> {
            OrderLineDto lineDto = new OrderLineDto();
            lineDto.id = line.getId();
            lineDto.productId = line.getProductId();
            lineDto.quantity = line.getQuantity();
            lineDto.linePrice = line.getLinePrice();
            return lineDto;
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