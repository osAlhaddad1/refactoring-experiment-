package com.example.shop.presentation;

import com.example.shop.application.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody CategoryDto category) {
        try {
            return shopService.createCategory(category);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/categories/{id}")
    public CategoryDto getCategory(@PathVariable Long id) {
        try {
            return shopService.getCategory(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/customers")
    public CustomerDto createCustomer(@RequestBody CustomerDto customer) {
        try {
            return shopService.createCustomer(customer);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/customers/{id}")
    public CustomerDto getCustomer(@PathVariable Long id) {
        try {
            return shopService.getCustomer(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/coupons")
    public CouponDto createCoupon(@RequestBody CouponDto coupon) {
        try {
            return shopService.createCoupon(coupon);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/products")
    public ProductDto createProduct(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            double price = ((Number) body.get("price")).doubleValue();
            int stock = ((Number) body.get("stock")).intValue();
            Object categoryIdObj = body.get("categoryId");
            Long categoryId = categoryIdObj != null ? ((Number) categoryIdObj).longValue() : null;
            return shopService.createProduct(name, price, stock, categoryId);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public ProductDto getProduct(@PathVariable Long id) {
        try {
            return shopService.getProduct(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders")
    public OrderDto placeOrder(@RequestBody OrderDto order) {
        try {
            return shopService.placeOrder(order);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        try {
            return shopService.getOrder(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/pay")
    public OrderDto payOrder(@PathVariable Long id) {
        try {
            return shopService.payOrder(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/ship")
    public OrderDto shipOrder(@PathVariable Long id) {
        try {
            return shopService.shipOrder(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderDto cancelOrder(@PathVariable Long id) {
        try {
            return shopService.cancelOrder(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        try {
            return shopService.invoiceOrder(id);
        } catch (NotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAudit();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }
}
