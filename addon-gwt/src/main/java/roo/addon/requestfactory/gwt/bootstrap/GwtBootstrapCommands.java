package roo.addon.requestfactory.gwt.bootstrap;

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

    @Reference private GwtBootstrapOperations operations;

    @CliAvailabilityIndicator({
            "web gwt bootstrap scaffold all",
            "web gwt bootstrap scaffold type",
            "web gwt bootstrap gae update" })
    public boolean isScaffoldAvailable() {
        return operations.isScaffoldAvailable();
    }

    @CliAvailabilityIndicator({ "web gwt bootstrap setup" })
    public boolean isGwtSetupAvailable() {
        return operations.isGwtInstallationPossible();
    }

    @CliAvailabilityIndicator({ "bootstrap entity" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    @CliCommand(value = "web gwt bootstrap setup", help = "Install Google Web Toolkit (GWT) Bootstrap into your project")
    public void webGwtBootstrapSetup() {
        operations.setupGwtBootstrap();
    }

    @CliCommand(value = "bootstrap entity", help = "Configure entity for GWT Bootstrap")
    public void add(@CliOption(key = "type", mandatory = true, help = "The entity to configure") JavaType target,
            @CliOption(key = RooGwtBootstrap.PARENT_PROPERTY_ATTRIBUTE, mandatory = false, help = "The name of the field of the parent") final JavaSymbolName parentProperty,
            @CliOption(key = RooGwtBootstrap.PRIMARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Primary property to be used when rendering") final JavaSymbolName primaryProperty,
            @CliOption(key = RooGwtBootstrap.SECONDARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Secondary property to be used when rendering") final JavaSymbolName secondaryProperty) {
        operations.annotateType(target, parentProperty, primaryProperty, secondaryProperty);
    }

    @CliCommand(value = "web gwt bootstrap scaffold all", help = "Locates all entities in the project and creates GWT requests, proxies and creates the scaffold")
    public void scaffoldAll(
            @CliOption(key = "proxyPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = "requestPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage requestPackage) {

        operations.scaffoldAll(proxyPackage, requestPackage);
    }

    @CliCommand(value = "web gwt bootstrap scaffold type", help = "Creates a GWT request, proxy and scaffold for the specified")
    public void scaffoldType(
            @CliOption(key = "proxyPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created proxies will be placed") final JavaPackage proxyPackage,
            @CliOption(key = "requestPackage", mandatory = true, optionContext = JavaTypeConverter.PROJECT, help = "The package in which created requests will be placed") final JavaPackage requestPackage,
            @CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type) {

        operations.scaffoldType(proxyPackage, requestPackage, type);
    }

    @CliCommand(value = "web gwt gae update", help = "Updates the GWT project to support GAE")
    public void updateGaeConfiguration() {
        operations.updateGaeConfiguration();
    }
}