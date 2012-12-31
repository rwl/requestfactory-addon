package org.springframework.roo.addon.requestfactory.gwt;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.gwt.RooRequestFactoryGwt;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class GwtCommands implements CommandMarker {

    private static final String SCAFFOLD_ALL_COMMAND = "web requestfactory gwt scaffold all";
    private static final String SCAFFOLD_TYPE_COMMAND = "web requestfactory gwt scaffold type";
    private static final String UPDATE_GAE_COMMAND = "web requestfactory gwt update gae";
    private static final String SETUP_GWT_COMMAND = "web requestfactory gwt setup";
    
    public static final String[] TAILORED_COMMANDS = new String[] {
        SCAFFOLD_ALL_COMMAND, SCAFFOLD_TYPE_COMMAND, UPDATE_GAE_COMMAND,
        SETUP_GWT_COMMAND
    };

    @Reference private GwtOperations operations;

    @CliAvailabilityIndicator({
            SCAFFOLD_ALL_COMMAND,
            SCAFFOLD_TYPE_COMMAND,
            UPDATE_GAE_COMMAND })
    public boolean isScaffoldAvailable() {
        return operations.isScaffoldAvailable();
    }

    @CliAvailabilityIndicator({ SETUP_GWT_COMMAND })
    public boolean isGwtSetupAvailable() {
        return operations.isGwtInstallationPossible();
    }

    @CliCommand(value = SETUP_GWT_COMMAND, help = "Install Google Web Toolkit (GWT) into your project")
    public void webGwtSetup() {
        operations.setupGwt();
    }

    @CliCommand(value = SCAFFOLD_ALL_COMMAND, help = "Locates all entities in the project and creates GWT requests, proxies and creates the scaffold")
    public void scaffoldAll(@CliOption(key = RooRequestFactoryGwt.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldAll(module);
    }

    @CliCommand(value = SCAFFOLD_TYPE_COMMAND, help = "Creates a GWT request, proxy and scaffold for the specified")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type,
            @CliOption(key = RooRequestFactoryGwt.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldType(type, module);
    }

    @CliCommand(value = UPDATE_GAE_COMMAND, help = "Updates the GWT project to support GAE")
    public void updateGaeConfiguration() {
        operations.updateGaeConfiguration();
    }
}