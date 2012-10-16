package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.converters.JavaTypeConverter;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class GwtBootstrapCommands implements CommandMarker {

    private static final String SCAFFOLD_ALL_COMMAND = "web requestfactory gwt bootstrap scaffold all";
    private static final String SCAFFOLD_TYPE_COMMAND = "web requestfactory gwt bootstrap scaffold type";
    private static final String SETUP_GAE_COMMAND = "web requestfactory setup gae";
    private static final String SETUP_GWT_BOOTSTRAP_COMMAND = "web requestfactory gwt bootstrap setup";

    @Reference private GwtBootstrapOperations operations;

    @CliAvailabilityIndicator({
            SCAFFOLD_ALL_COMMAND,
            SCAFFOLD_TYPE_COMMAND,
            SETUP_GAE_COMMAND })
    public boolean isScaffoldAvailable() {
        return operations.isScaffoldAvailable();
    }

    @CliAvailabilityIndicator({ SETUP_GWT_BOOTSTRAP_COMMAND })
    public boolean isGwtSetupAvailable() {
        return operations.isGwtInstallationPossible();
    }

    @CliCommand(value = SETUP_GWT_BOOTSTRAP_COMMAND, help = "Install Google Web Toolkit (GWT) Bootstrap into your project")
    public void webGwtBootstrapSetup() {
        operations.setupGwtBootstrap();
    }

    @CliCommand(value = SCAFFOLD_ALL_COMMAND, help = "Locates all entities in the project and creates GWT requests, proxies and creates the scaffold")
    public void scaffoldAll(
            @CliOption(key = "proxyPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = "requestPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage requestPackage) {

        operations.scaffoldAll(proxyPackage, requestPackage);
    }

    @CliCommand(value = SCAFFOLD_TYPE_COMMAND, help = "Creates a GWT request, proxy and scaffold for the specified")
    public void scaffoldType(
            @CliOption(key = "proxyPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = "requestPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage requestPackage,
            @CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type) {

        operations.scaffoldType(proxyPackage, requestPackage, type);
    }

    @CliCommand(value = SETUP_GAE_COMMAND, help = "Updates the GWT project to support GAE")
    public void updateGaeConfiguration() {
        operations.updateGaeConfiguration();
    }
}