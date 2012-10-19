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

    private static final String TYPE_KEY = "TYPE";
    private static final String PACKAGE_KEY = "package";
    private static final String PROXY_PACKAGE_KEY = "proxyPackage";
    private static final String REQUEST_PACKAGE_KEY = "requestPackage";

    private static final String WEB_REQUEST_FACTORY_SETUP_SERVER_COMMAND = "web requestfactory setup server";
    private static final String WEB_REQUEST_FACTORY_SETUP_CLIENT_COMMAND = "web requestfactory setup client";
    private static final String WEB_REQUEST_FACTORY_PROXY_ALL_COMMAND = "web requestfactory proxy all";
    private static final String WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND = "web requestfactory proxy type";
    private static final String WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND = "web requestfactory request all";
    private static final String WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND = "web requestfactory request type";
    private static final String WEB_REQUEST_FACTORY_PROXY_REQUEST_ALL_COMMAND = "web requestfactory proxy request all";
    private static final String WEB_REQUEST_FACTORY_PROXY_REQUEST_TYPE_COMMAND = "web requestfactory proxy request type";
    private static final String WEB_REQUEST_FACTORY_SCAFFOLD_ALL_COMMAND = "web requestfactory scaffold all";
    private static final String WEB_REQUEST_FACTORY_SCAFFOLD_TYPE_COMMAND = "web requestfactory scaffold type";

    @Reference protected RequestFactoryOperations requestFactoryOperations;

    @CliAvailabilityIndicator({ WEB_REQUEST_FACTORY_SETUP_SERVER_COMMAND })
    public boolean isRequestFactoryServerSetupAvailable() {
        return requestFactoryOperations.isRequestFactoryServerInstallationPossible();
    }

    @CliAvailabilityIndicator({ WEB_REQUEST_FACTORY_SETUP_CLIENT_COMMAND })
    public boolean isRequestFactoryClientSetupAvailable() {
        return requestFactoryOperations.isRequestFactoryClientInstallationPossible();
    }

    @CliAvailabilityIndicator({
            WEB_REQUEST_FACTORY_PROXY_ALL_COMMAND,
            WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND,
            WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND,
            WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND,
            WEB_REQUEST_FACTORY_PROXY_REQUEST_ALL_COMMAND,
            WEB_REQUEST_FACTORY_PROXY_REQUEST_TYPE_COMMAND
    })
    public boolean isRequestFactoryCommandAvailable() {
        return requestFactoryOperations.isRequestFactoryCommandAvailable();
    }

    @CliAvailabilityIndicator({
            WEB_REQUEST_FACTORY_SCAFFOLD_ALL_COMMAND,
            WEB_REQUEST_FACTORY_SCAFFOLD_TYPE_COMMAND
    })
    public boolean isScaffoldAvailable() {
        return requestFactoryOperations.isScaffoldAvailable();
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
            @CliOption(key = PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.proxy", help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate locators") final Pom locatorModule) {

        requestFactoryOperations.proxyAll(javaPackage, locatorModule);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_PROXY_TYPE_COMMAND, help = "Creates a GWT proxy based on the specified type")
    public void proxyType(
            @CliOption(key = PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.proxy", help = "The package in which created proxies will be placed") final JavaPackage javaPackage,
            @CliOption(key = TYPE_KEY, mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type,
            @CliOption(key = RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate locators") final Pom locatorModule) {

        requestFactoryOperations.proxyType(javaPackage, type, locatorModule);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_REQUEST_ALL_COMMAND, help = "Locates all entities in the project and creates GWT requests")
    public void requestAll(
            @CliOption(key = PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.request", help = "The package in which created requests will be placed") final JavaPackage javaPackage) {

        requestFactoryOperations.requestAll(javaPackage);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_REQUEST_TYPE_COMMAND, help = "Creates a GWT request based on the specified type")
    public void requestType(
            @CliOption(key = PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.request", help = "The package in which created requests will be placed") final JavaPackage javaPackage,
            @CliOption(key = TYPE_KEY, mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created request on") final JavaType type) {

        requestFactoryOperations.requestType(javaPackage, type);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_PROXY_REQUEST_ALL_COMMAND, help = "Locates all entities in the project and creates GWT requests and proxies")
    public void proxyAndRequestAll(
            @CliOption(key = PROXY_PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.proxy", help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = REQUEST_PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.request", help = "The package in which created requests will be placed") final JavaPackage requestPackage,
            @CliOption(key = RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate server-side artifacts") final Pom serverModule) {

        requestFactoryOperations.proxyAll(proxyPackage, serverModule);
        requestFactoryOperations.requestAll(requestPackage);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_PROXY_REQUEST_TYPE_COMMAND, help = "Creates a proxy and request based on the specified type")
    public void proxyAndRequestType(
            @CliOption(key = PROXY_PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.proxy", help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = REQUEST_PACKAGE_KEY, mandatory = false, optionContext = JavaTypeConverter.PROJECT, unspecifiedDefaultValue = "~.client.request", help = "The package in which created requests will be placed") final JavaPackage requestPackage,
            @CliOption(key = TYPE_KEY, mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The type to base the created proxy and request on") final JavaType type,
            @CliOption(key = RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate server-side artifacts") final Pom serverModule) {

        requestFactoryOperations.proxyType(proxyPackage, type, serverModule);
        requestFactoryOperations.requestType(requestPackage, type);
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_SCAFFOLD_ALL_COMMAND, help = "Locates all proxies in the project and creates the shared scaffold")
    public void scaffoldAll() {
        requestFactoryOperations.scaffoldAll();
    }

    @CliCommand(value = WEB_REQUEST_FACTORY_SCAFFOLD_TYPE_COMMAND, help = "Creates shared scaffold for the specified type")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type) {
        requestFactoryOperations.scaffoldType(type);
    }
}