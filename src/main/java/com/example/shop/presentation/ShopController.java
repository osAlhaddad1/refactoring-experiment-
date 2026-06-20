package com.example.shop.presentation;

import com.example.shop.application.ShopService;
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
    public CustomerDto createCustomer(@RequestBody CustomerDto customer) {
        com.example.shop.application.CustomerDto appDto = new com.example.shop.application.CustomerDto(
            customer.getId(), customer.getName(), customer.getLoyaltyPoints()
        );
        com.example.shop.application.CustomerDto saved = shopService.createCustomer(appDto);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        com.example.shop.application.CustomerDto saved = shopService.getCustomer(id);
        return new CustomerDto(saved.getId(), saved.getName(), saved.getLoyaltyPoints());
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody ProductDto product) {
        com.example.shop.application.ProductDto appDto = new com.example.shop.application.ProductDto(
            product.getId(), product.getName(), product.getPrice(), product.getStock()
        );
        com.example.shop.application.ProductDto saved = shopService.createProduct(appDto);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        com.example.shop.application.ProductDto saved = shopService.getProduct(id);
        return new ProductDto(saved.getId(), saved.getName(), saved.getPrice(), saved.getStock());
    }

    @PostMapping("/orders")
    public OrderHeaderDto placeOrder(@RequestBody OrderHeaderDto order) {
        com.example.shop.application.OrderHeaderDto appDto = new com.example.shop.application.OrderHeaderDto();
        appDto.setCustomerId(order.getCustomerId());
        appDto.setLines(order.getLines().stream().map(line -> new com.example.shop.application.OrderLineDto(
            line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()
        )).collect(Collectors.toList()));

        com.example.shop.application.OrderHeaderDto saved = shopService.placeOrder(appDto);
        return mapToPresentationDto(saved);
    }

    @GetMapping("/orders/{id}")
    public OrderHeaderDto getOrder(@PathVariable Long id) {
        com.example.shop.application.OrderHeaderDto saved = shopService.getOrder(id);
        return mapToPresentationDto(saved);
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeaderDto payOrder(@PathVariable Long id) {
        com.example.shop.application.OrderHeaderDto saved = shopService.payOrder(id);
        return mapToPresentationDto(saved);
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeaderDto shipOrder(@PathVariable Long id) {
        com.example.shop.application.OrderHeaderDto saved = shopService.shipOrder(id);
        return mapToPresentationDto(saved);
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeaderDto cancelOrder(@PathVariable Long id) {
        com.example.shop.application.OrderHeaderDto saved = shopService.cancelOrder(id);
        return mapToPresentationDto(saved);
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    private OrderHeaderDto mapToPresentationDto(com.example.shop.application.OrderHeaderDto appDto) {
        List<OrderLineDto> lines = appDto.getLines().stream().map(line -> new OrderLineDto(
            line.getId(), line.getProductId(), line.getQuantity(), line.getLinePrice()
        )).collect(Collectors.toList());
        return new OrderHeaderDto(appDto.getId(), appDto.getCustomerId(), appDto.getStatus(), appDto.getTotal(), lines);
    }
}