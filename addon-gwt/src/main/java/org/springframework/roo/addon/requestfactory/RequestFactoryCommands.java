package org.springframework.roo.addon.requestfactory;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.converters.JavaTypeConverter;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;
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

    private static final String WEB_REQUEST_FACTORY_SETUP_SERVER_COMMAND = "web requestfactory setup server";
    private static final String WEB_REQUEST_FACTORY_SETUP_CLIENT_COMMAND = "web requestfactory setup client";
    private static final String WEB_REQUEST_FACTORY_PROXY_ALL_COMMAND = "web requestfactory proxy all";
    private static final String WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND = "web requestfactory proxy type";
    private static final String WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND = "web requestfactory request all";
    private static final String WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND = "web requestfactory request type";

    @Reference protected RequestFactoryOperations requestFactoryOperations;

    @CliAvailabilityIndicator({ WEB_REQUEST_FACTORY_SETUP_SERVER_COMMAND })
    public boolean isRequestFactoryServerSetupAvailable() {
        return requestFactoryOperations.isRequestFactoryServerInstallationPossible();
    }

    @CliAvailabilityIndicator({ WEB_REQUEST_FACTORY_SETUP_CLIENT_COMMAND })
    public boolean isRequestFactoryClientSetupAvailable() {
        return requestFactoryOperations.isRequestFactoryClientInstallationPossible();
    }

    @CliAvailabilityIndicator({ WEB_REQUEST_FACTORY_PROXY_ALL_COMMAND,
            WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND,
            WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND,
            WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND })
    public boolean isScaffoldAvailable() {
        return requestFactoryOperations.isRequestFactoryCommandAvailable();
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_SETUP_SERVER_COMMAND, help = "Install RequestFactory server into your project")
    public void webRequestFactoryServerSetup() {
        requestFactoryOperations.setupRequestFactoryServer();
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_SETUP_CLIENT_COMMAND, help = "Install RequestFactory client into your project")
    public void webRequestFactoryClientSetup() {
        requestFactoryOperations.setupRequestFactoryClient();
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_PROXY_ALL_COMMAND, help = "Locates all entities in the project and creates GWT proxies")
    public void proxyAll(
            @CliOption(key = "package", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = RooRequestFactoryProxy.LOCATOR_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate locators") final Pom module) {

        requestFactoryOperations.proxyAll(javaPackage, module);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND, help = "Creates a GWT proxy based on the specified type")
    public void proxyType(
            @CliOption(key = "package", mandatory = true, help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = "type", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type,
            @CliOption(key = RooRequestFactoryProxy.LOCATOR_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate locators") final Pom module) {

        requestFactoryOperations.proxyType(javaPackage, type, module);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND, help = "Locates all entities in the project and creates GWT requests")
    public void requestAll(
            @CliOption(key = "package", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage javaPackage) {

        requestFactoryOperations.requestAll(javaPackage);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND, help = "Creates a GWT request based on the specified type")
    public void requestType(
            @CliOption(key = "package", mandatory = true, help = "The package in which created requests will be placed") final JavaPackage javaPackage,
            @CliOption(key = "type", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type) {

        requestFactoryOperations.requestType(javaPackage, type);
    }
}