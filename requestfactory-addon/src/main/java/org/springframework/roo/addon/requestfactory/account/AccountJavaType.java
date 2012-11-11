package org.springframework.roo.addon.requestfactory.account;

import org.springframework.roo.addon.requestfactory.annotations.account.RooAccount;
import org.springframework.roo.model.JavaType;

public final class AccountJavaType {

    public static final JavaType ROO_ACCOUNT = new JavaType(
            RooAccount.class);

    public static final JavaType SIMPLE_GRANTED_AUTHORITY = new JavaType(
            "org.springframework.security.core.authority.SimpleGrantedAuthority");

    public static final JavaType USER_DETAILS = new JavaType(
            "org.springframework.security.core.userdetails.UserDetails");

    /**
     * Constructor is private to prevent instantiation
     */
    private AccountJavaType() {
    }
}
