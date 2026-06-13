package com.example.shop.infrastructure;

import com.example.shop.application.ShopApplicationService;
import com.example.shop.domain.AuditPort;
import com.example.shop.domain.CustomerRepository;
import com.example.shop.domain.OrderRepository;
import com.example.shop.domain.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShopConfig {
    @Bean
    public ShopApplicationService shopApplicationService(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            AuditPort auditPort) {
        return new ShopApplicationService(customerRepository, productRepository, orderRepository, auditPort);
    }
}
