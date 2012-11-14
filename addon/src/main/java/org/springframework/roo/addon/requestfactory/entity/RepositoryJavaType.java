package org.springframework.roo.addon.requestfactory.entity;

import org.springframework.roo.model.JavaType;

public final class RepositoryJavaType {

    public static final JavaType PAGEABLE = new JavaType(
            "org.springframework.data.domain.Pageable");
    public static final JavaType PAGE = new JavaType(
            "org.springframework.data.domain.Page");

    /**
     * Constructor is private to prevent instantiation
     */
    private RepositoryJavaType() {
    }
}
