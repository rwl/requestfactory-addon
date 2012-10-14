package roo.addon.account;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type or an external add-on.
 *
 * @since 1.1
 */
public interface AccountOperations {

    String SECURITY_FILTER_NAME = "springSecurityFilterChain";

    /**
     * Indicate commands should be available
     *
     * @return true if it should be available, otherwise false
     */
    boolean isAddCommandAvailable();

    boolean isSetupCommandAvailable();

    /**
     * Annotate the provided Java type with the trigger of this add-on
     */
    void annotateAccountType(JavaType type, final JavaPackage sharedPackage);

    void setupSecurity(JavaType type, final JavaPackage accountPackage);
}