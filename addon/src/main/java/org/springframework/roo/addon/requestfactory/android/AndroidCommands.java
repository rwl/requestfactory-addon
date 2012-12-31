package org.springframework.roo.addon.requestfactory.android;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.RequestFactoryCommands;
import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class AndroidCommands implements CommandMarker {

    private static final String SETUP_ANDROID_COMMAND = RequestFactoryCommands.WEB_REQUEST_FACTORY_PREFIX + " android setup";
    
    private static final String SCAFFOLD_ALL_COMMAND = RequestFactoryCommands.WEB_REQUEST_FACTORY_PREFIX + " android scaffold all";
    private static final String SCAFFOLD_TYPE_COMMAND = RequestFactoryCommands.WEB_REQUEST_FACTORY_PREFIX + " android scaffold type";
    
    public static final String[] TAILORED_COMMANDS = new String[] {
        SETUP_ANDROID_COMMAND, SCAFFOLD_ALL_COMMAND, SCAFFOLD_TYPE_COMMAND
    };

    @Reference private AndroidOperations operations;

    @CliCommand(value = SETUP_ANDROID_COMMAND, help = "Setup module to use RequestFactory with Android")
    public void androidSetup() {
        operations.setupAndroid();
    }

    @CliAvailabilityIndicator({
            SCAFFOLD_ALL_COMMAND,
            SCAFFOLD_TYPE_COMMAND})
    public boolean isScaffoldAvailable() {
        return operations.isScaffoldAvailable();
    }

    @CliCommand(value = SCAFFOLD_ALL_COMMAND, help = "Locates all entities in the project and creates the scaffold")
    public void scaffoldAll(@CliOption(key = RooRequestFactoryAndroid.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldAll(module);
    }

    @CliCommand(value = SCAFFOLD_TYPE_COMMAND, help = "Creates Android scaffold for the specified type")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created scaffold on") final JavaType type,
            @CliOption(key = RooRequestFactoryAndroid.MODULE_ATTRIBUTE, mandatory = false, help = "The module in which to generate scaffold artifacts") final Pom module) {
        operations.scaffoldType(type, module);
    }
}
