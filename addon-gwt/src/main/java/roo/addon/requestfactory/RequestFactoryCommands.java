package roo.addon.requestfactory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.converters.JavaTypeConverter;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Commands for the GWT add-on to be used by the Roo shell.
 *
 * @author Ben Alex
 * @author James Tyrrell
 * @since 1.1
 */
@Component
@Service
public class RequestFactoryCommands implements CommandMarker {

    @Reference protected RequestFactoryOperations requestFactoryOperations;

    @CliAvailabilityIndicator({ "web requestfactory setup" })
    public boolean isRequestFactorySetupAvailable() {
        return requestFactoryOperations.isRequestFactoryServerInstallationPossible();
    }

    @CliAvailabilityIndicator({ "web requestfactory locator all",
            "web requestfactory locator type",
            "web requestfactory proxy all",
            "web requestfactory proxy type",
            "web requestfactory request all",
            "web requestfactory request type" })
    public boolean isScaffoldAvailable() {
        return requestFactoryOperations.isRequestFactoryCommandAvailable();
    }

    @CliCommand(value = "web requestfactory setup", help = "Install GWT RequestFactory into your project")
    public void webRequestFactorySetup() {
        requestFactoryOperations.setupRequestFactoryServer();
    }

    @CliCommand(value = "web requestfactory locator all", help = "Locates all entities in the project and creates RequestFactory locators")
    public void locatorAll(
            @CliOption(key = "package", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage javaPackage) {

        requestFactoryOperations.locatorAll(javaPackage);
    }

    @CliCommand(value = "web requestfactory locator type", help = "Creates a RequestFactory locator based on the specified type")
    public void locatorType(
            @CliOption(key = "package", mandatory = true, help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = "type", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type) {

        requestFactoryOperations.locatorType(javaPackage, type);
    }

    @CliCommand(value = "web requestfactory proxy all", help = "Locates all entities in the project and creates GWT proxies")
    public void proxyAll(
            @CliOption(key = "package", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage javaPackage) {

        requestFactoryOperations.proxyAll(javaPackage);
    }

    @CliCommand(value = "web requestfactory proxy type", help = "Creates a GWT proxy based on the specified type")
    public void proxyType(
            @CliOption(key = "package", mandatory = true, help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = "type", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type) {

        requestFactoryOperations.proxyType(javaPackage, type);
    }

    @CliCommand(value = "web requestfactory request all", help = "Locates all entities in the project and creates GWT requests")
    public void requestAll(
            @CliOption(key = "package", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage javaPackage) {

        requestFactoryOperations.requestAll(javaPackage);
    }

    @CliCommand(value = "web requestfactory request type", help = "Creates a GWT proxy based on the specified type")
    public void requestType(
            @CliOption(key = "package", mandatory = true, help = "The package in which created requests will be placed") final JavaPackage javaPackage,
            @CliOption(key = "type", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type) {

        requestFactoryOperations.requestType(javaPackage, type);
    }
}