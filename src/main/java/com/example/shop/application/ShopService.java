package com.example.shop.application;

import com.example.shop.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
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

    public CustomerDto createCustomer(String name) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setLoyaltyPoints(0);
        Customer saved = customerRepository.save(customer);
        return mapToDto(saved);
    }

    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("customer not found"));
        return mapToDto(customer);
    }

    public ProductDto createProduct(String name, double price, int stock) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    public ProductDto getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("product not found"));
        return mapToDto(product);
    }

    public OrderHeaderDto placeOrder(Long customerId, List<OrderLineInput> lineInputs) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("customer not found"));

        if (lineInputs == null || lineInputs.isEmpty()) {
            throw new BadRequestException("order has no lines");
        }

        OrderHeader order = new OrderHeader();
        order.setCustomerId(customerId);
        order.setStatus("NEW");

        double subtotal = 0;
        for (OrderLineInput input : lineInputs) {
            Product product = productRepository.findById(input.getProductId())
                    .orElseThrow(() -> new NotFoundException("product not found"));

            if (input.getQuantity() <= 0) {
                throw new BadRequestException("quantity must be positive");
            }
            if (product.getStock() < input.getQuantity()) {
                throw new BadRequestException("not enough stock");
            }

            double linePrice = product.getPrice() * input.getQuantity();
            if (input.getQuantity() >= 10) {
                linePrice = linePrice * 90 / 100;
            }

            OrderLine line = new OrderLine();
            line.setProductId(product.getId());
            line.setQuantity(input.getQuantity());
            line.setLinePrice(linePrice);
            order.getLines().add(line);

            subtotal += linePrice;

            product.setStock(product.getStock() - input.getQuantity());
            productRepository.save(product);
        }

        double total = subtotal;
        if (subtotal >= 500) {
            total = total * 95 / 100;
        }
        order.setTotal(total);

        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.record("created order " + savedOrder.getId());
        return mapToDto(savedOrder);
    }

    public OrderHeaderDto getOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));
        return mapToDto(order);
    }

    public OrderHeaderDto payOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        if (!"NEW".equals(order.getStatus())) {
            throw new BadRequestException("only NEW orders can be paid");
        }
        order.setStatus("PAID");

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new NotFoundException("customer not found"));
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + (int) order.getTotal());
        customerRepository.save(customer);

        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.record("paid order " + savedOrder.getId());
        return mapToDto(savedOrder);
    }

    public OrderHeaderDto shipOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        if (!"PAID".equals(order.getStatus())) {
            throw new BadRequestException("only PAID orders can be shipped");
        }
        order.setStatus("SHIPPED");

        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.record("shipped order " + savedOrder.getId());
        return mapToDto(savedOrder);
    }

    public OrderHeaderDto cancelOrder(Long id) {
        OrderHeader order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("order not found"));

        if ("SHIPPED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            throw new BadRequestException("cannot cancel a " + order.getStatus() + " order");
        }

        for (OrderLine line : order.getLines()) {
            Product product = productRepository.findById(line.getProductId()).orElse(null);
            if (product != null) {
                product.setStock(product.getStock() + line.getQuantity());
                productRepository.save(product);
            }
        }
        order.setStatus("CANCELLED");

        OrderHeader savedOrder = orderRepository.save(order);
        auditLog.record("cancelled order " + savedOrder.getId());
        return mapToDto(savedOrder);
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
        if (order.getLines() != null) {
            dto.lines = order.getLines().stream().map(line -> {
                OrderLineDto ld = new OrderLineDto();
                ld.id = line.getId();
                ld.productId = line.getProductId();
                ld.quantity = line.getQuantity();
                ld.linePrice = line.getLinePrice();
                return ld;
            }).collect(Collectors.toList());
        }
        return dto;
    }
}
