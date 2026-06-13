package com.example.shop.domain;

public class EntityNotFoundException extends RuntimeException {
    private final String entityName;

    public EntityNotFoundException(String entityName) {
        super(entityName + " not found");
        this.entityName = entityName;
    }

    public String getEntityName() { return entityName; }
}
