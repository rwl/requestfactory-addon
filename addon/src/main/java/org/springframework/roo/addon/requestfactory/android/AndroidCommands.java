package org.springframework.roo.addon.requestfactory.android;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.model.JavaSymbolName;
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

    private static final String ACTIVITY_COMMAND = "android activity";
    private static final String VIEW_COMMAND = "android view";
    private static final String RESOURCE_STRING_COMMAND = "android resource string";

    @Reference private AndroidOperations operations;
    @Reference private AndroidProjectOperations projectOperations;

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


    @CliAvailabilityIndicator({ ACTIVITY_COMMAND })
    public boolean isActivityAvailable() {
        return projectOperations.isActivityAvailable();
    }

    @CliAvailabilityIndicator({ VIEW_COMMAND, RESOURCE_STRING_COMMAND })
    public boolean isViewAvailable() {
        return projectOperations.isViewAvailable();
    }
    
    @CliCommand(value = ACTIVITY_COMMAND, help = "Creates a new Android activity in SRC_MAIN_JAVA")
    public void activity(@CliOption(key = "class", optionContext = "update,project", mandatory = true, help = "Name of the activity to create") final JavaType name,
            @CliOption(key = "layout", mandatory = false, help = "Name of the layout to create under /res/layout and pass to setContentView() in onCreate()") final String layout) {
        projectOperations.activity(name, layout);
    }
    
    public void view(@CliOption(key = { "", "identifier" }, mandatory = true, help = "The ID of the view to add") final String identifier,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The type to receive this view") final JavaType typeName,
            @CliOption(key = "view", mandatory = true, help = "Class name of the view to create") final JavaType view,
            @CliOption(key = "fieldName", mandatory = false, help = "The name of the field to add (defaults to the view ID)") final JavaSymbolName fieldName,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The height of the view group") final Dimension height,
            @CliOption(key = "width", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The width of the view group") final Dimension width) {
        projectOperations.view(typeName, view, identifier, fieldName, height, width);
    }
    
    public void resourceString(@CliOption(key = { "", "name" }, mandatory = true, help = "The name of the resource to add") final String name,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The type to receive this resource") final JavaType typeName,
            @CliOption(key = "value", mandatory = true, help = "The content of the string resource") final String value,
            @CliOption(key = "fieldName", mandatory = false, help = "The name of the field to add (defaults to the resource name)") final JavaSymbolName fieldName,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "WRAP_CONTENT", help = "The height of the view") final Dimension height,
            @CliOption(key = "width", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The width of the view") final Dimension width) {
        projectOperations.resourceString(typeName, name, fieldName, value, height, width);
    }
}
