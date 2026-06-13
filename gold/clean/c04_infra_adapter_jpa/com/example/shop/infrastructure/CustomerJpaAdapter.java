package com.example.shop.infrastructure;

import com.example.shop.domain.CustomerPort;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

// CLEAN: infrastructure may use JPA and implements the domain port.
public class CustomerJpaAdapter implements CustomerPort {

    public String nameOf(long id) {
        return "name-" + id;
    }

    @Entity
    public static class CustomerEntity {
        @Id
        public Long id;
        public String name;
    }
}
