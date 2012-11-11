package org.springframework.roo.addon.requestfactory.account;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.account.RooAccount;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on
 * non-Windows systems).
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class AccountCommands implements CommandMarker { // All command types must implement the CommandMarker interface

    /**
     * Get a reference to the AccountOperations from the underlying OSGi container
     */
    @Reference private AccountOperations operations;

    /**
     * This method is optional. It allows automatic command hiding in situations when the command should not be visible.
     * For example the 'entity' command will not be made available before the user has defined his persistence settings
     * in the Roo shell or directly in the project.
     *
     * You can define multiple methods annotated with {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     *
     * @return true (default) if the command should be visible at this stage, false otherwise
     */
    @CliAvailabilityIndicator({ "account add" })
    public boolean isAddCommandAvailable() {
        return operations.isAddCommandAvailable();
    }

    @CliAvailabilityIndicator({ "account setup" })
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    @CliCommand(value = "account add", help = "Configure entity as a Spring Security account")
    public void addAccount(@CliOption(key = "type", mandatory = true, help = "The account entity") JavaType target,
            @CliOption(key = RooAccount.SHARED_PACKAGE_ATTRIBUTE, mandatory = false, help = "Package for enums shared with GWT") final JavaPackage sharedPackage) {
        operations.annotateAccountType(target, sharedPackage);
    }

    @CliCommand(value = "account setup", help = "Setup Spring Security account service")
    public void setupSecurity(@CliOption(key = "account", mandatory = true, help = "The account entity") JavaType target,
            @CliOption(key = "package", mandatory = true, help = "Package for user details service") final JavaPackage accountPackage) {
        operations.setupSecurity(target, accountPackage);
    }
}