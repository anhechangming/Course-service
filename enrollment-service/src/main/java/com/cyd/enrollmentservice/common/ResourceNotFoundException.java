package com.cyd.enrollmentservice.common;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " not found with id: " + resourceId);
    }
}