package com.example.shop.presentation;

import com.example.shop.application.ShopService;
import com.example.shop.domain.*;
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
    public Category createCategory(@RequestBody Category category) {
        try {
            return shopService.createCategory(category);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @GetMapping("/categories/{id}")
    public Category getCategory(@PathVariable Long id) {
        try {
            return shopService.getCategory(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/customers")
    public Customer createCustomer(@RequestBody Customer customer) {
        try {
            return shopService.createCustomer(customer);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomer(@PathVariable Long id) {
        try {
            return shopService.getCustomer(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/coupons")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        try {
            return shopService.createCoupon(coupon);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody Map<String, Object> body) {
        try {
            String name = (String) body.get("name");
            double price = ((Number) body.get("price")).doubleValue();
            int stock = ((Number) body.get("stock")).intValue();
            Object categoryIdObj = body.get("categoryId");
            Long categoryId = categoryIdObj != null ? ((Number) categoryIdObj).longValue() : null;
            return shopService.createProduct(name, price, stock, categoryId);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        try {
            return shopService.getProduct(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/orders")
    public OrderHeader placeOrder(@RequestBody OrderHeader order) {
        try {
            return shopService.placeOrder(order);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @GetMapping("/orders/{id}")
    public OrderHeader getOrder(@PathVariable Long id) {
        try {
            return shopService.getOrder(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/pay")
    public OrderHeader payOrder(@PathVariable Long id) {
        try {
            return shopService.payOrder(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/ship")
    public OrderHeader shipOrder(@PathVariable Long id) {
        try {
            return shopService.shipOrder(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/cancel")
    public OrderHeader cancelOrder(@PathVariable Long id) {
        try {
            return shopService.cancelOrder(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @PostMapping("/orders/{id}/invoice")
    public Map<String, Object> invoiceOrder(@PathVariable Long id) {
        try {
            return shopService.invoiceOrder(id);
        } catch (EntityNotFoundException e) {
            throw notFound(e.getMessage());
        } catch (DomainException e) {
            throw badRequest(e.getMessage());
        }
    }

    @GetMapping("/audit")
    public List<String> audit() {
        return shopService.getAuditLogs();
    }

    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        return shopService.getMetrics();
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found");
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
