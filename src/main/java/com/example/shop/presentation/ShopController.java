package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/customers")
    public CustomerResponse createCustomer(@RequestBody CreateCustomerRequest request) {
        CustomerDto dto = shopService.createCustomer(request.name);
        return mapToResponse(dto);
    }

    @GetMapping("/customers/{id}")
    public CustomerResponse getCustomer(@PathVariable Long id) {
        CustomerDto dto = shopService.getCustomer(id);
        return mapToResponse(dto);
    }

    @PostMapping("/products")
    public ProductResponse createProduct(@RequestBody CreateProductRequest request) {
        ProductDto dto = shopService.createProduct(request.name, request.price, request.stock);
        return mapToResponse(dto);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        ProductDto dto = shopService.getProduct(id);
        return mapToResponse(dto);
    }

    @PostMapping("/orders")
    public OrderResponse placeOrder(@RequestBody PlaceOrderRequest request) {
        List<OrderLineInput> inputs = null;
        if (request.lines != null) {
            inputs = request.lines.stream()
                    .map(line -> new OrderLineInput(line.productId, line.quantity))
                    .collect(Collectors.toList());
        }
        OrderHeaderDto dto = shopService.placeOrder(request.customerId, inputs);
        return mapToResponse(dto);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        OrderHeaderDto dto = shopService.getOrder(id);
        return mapToResponse(dto);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderResponse payOrder(@PathVariable Long id) {
        OrderHeaderDto dto = shopService.payOrder(id);
        return mapToResponse(dto);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderResponse shipOrder(@PathVariable Long id) {
        OrderHeaderDto dto = shopService.shipOrder(id);
        return mapToResponse(dto);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        OrderHeaderDto dto = shopService.cancelOrder(id);
        return mapToResponse(dto);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    private CustomerResponse mapToResponse(CustomerDto dto) {
        CustomerResponse response = new CustomerResponse();
        response.id = dto.id;
        response.name = dto.name;
        response.loyaltyPoints = dto.loyaltyPoints;
        return response;
    }

    private ProductResponse mapToResponse(ProductDto dto) {
        ProductResponse response = new ProductResponse();
        response.id = dto.id;
        response.name = dto.name;
        response.price = dto.price;
        response.stock = dto.stock;
        return response;
    }

    private OrderResponse mapToResponse(OrderHeaderDto dto) {
        OrderResponse response = new OrderResponse();
        response.id = dto.id;
        response.customerId = dto.customerId;
        response.status = dto.status;
        response.total = dto.total;
        if (dto.lines != null) {
            response.lines = dto.lines.stream().map(line -> {
                OrderLineResponse lr = new OrderLineResponse();
                lr.id = line.id;
                lr.productId = line.productId;
                lr.quantity = line.quantity;
                lr.linePrice = line.linePrice;
                return lr;
            }).collect(Collectors.toList());
        }
        return response;
    }
}
