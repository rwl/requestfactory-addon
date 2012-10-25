package org.springframework.roo.addon.requestfactory.android;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class AndroidCommands implements CommandMarker {

    private static final String SCAFFOLD_ALL_COMMAND = "web requestfactory android scaffold all";
    private static final String SCAFFOLD_TYPE_COMMAND = "web requestfactory android scaffold type";

    @Reference private AndroidOperations operations;

    @CliAvailabilityIndicator({
            SCAFFOLD_ALL_COMMAND,
            SCAFFOLD_TYPE_COMMAND})
    public boolean isScaffoldAvailable() {
        return operations.isScaffoldAvailable();
    }

    @CliCommand(value = SCAFFOLD_ALL_COMMAND, help = "Locates all entities in the project and creates the scaffold")
    public void scaffoldAll(@CliOption(key = RooAndroidScaffold.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldAll(module);
    }

    @CliCommand(value = SCAFFOLD_TYPE_COMMAND, help = "Creates Android scaffold for the specified type")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type,
            @CliOption(key = RooAndroidScaffold.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldType(type, module);
    }
}