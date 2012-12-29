package org.springframework.roo.addon.requestfactory.android.project;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.android.types.Dimension;
import org.springframework.roo.addon.requestfactory.android.types.Orientation;
import org.springframework.roo.addon.requestfactory.android.types.Permission;
import org.springframework.roo.addon.requestfactory.android.types.SystemService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class AndroidProjectCommands implements CommandMarker {

    private static final String LAYOUT_COMMAND = "android layout";
    private static final String ACTIVITY_COMMAND = "android activity";
    private static final String VIEW_COMMAND = "android view";
    private static final String RESOURCE_STRING_COMMAND = "android resource string";
    private static final String SYSTEM_SERVICE_COMMAND = "android service";
    private static final String PERMISSION_COMMAND = "android permission";
    private static final String FRAGMENT_COMMAND = "android fragment";

    @Reference private AndroidProjectOperations projectOperations;

    @CliAvailabilityIndicator({ ACTIVITY_COMMAND, LAYOUT_COMMAND })
    public boolean isActivityAvailable() {
        return projectOperations.isActivityAvailable();
    }

    @CliAvailabilityIndicator({ VIEW_COMMAND, RESOURCE_STRING_COMMAND, SYSTEM_SERVICE_COMMAND })
    public boolean isViewAvailable() {
        return projectOperations.isViewAvailable();
    }

    @CliCommand(value = LAYOUT_COMMAND, help = "Creates a layout")
    public void layout(@CliOption(key = { "", "name" }, mandatory = true, help = "The name of the layout to add") final String name,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The height of the view group") final Dimension height,
            @CliOption(key = "width", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The width of the view group") final Dimension width,
            @CliOption(key = "orientation", mandatory = false, unspecifiedDefaultValue = "VERTICAL", help = "Should the layout be a column or a row?") final Orientation orientation) {
        projectOperations.layout(name, height, width, orientation);
    }
    
    @CliCommand(value = ACTIVITY_COMMAND, help = "Creates a new Android activity")
    public void activity(@CliOption(key = "class", optionContext = "update,project", mandatory = true, help = "Name of the activity to create") final JavaType name,
            @CliOption(key = "layout", mandatory = false, unspecifiedDefaultValue = "", help = "Name of the layout to create under /res/layout and pass to setContentView() in onCreate()") final String layout,
            @CliOption(key = "launcher", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether the activity is the main launch activity") final boolean main,
            @CliOption(key = "noTitle", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Requests a window feature with no title") final boolean noTitle,
            @CliOption(key = "fullscreen", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Flags the window as fullscreen") final boolean fullscreen) {
        projectOperations.activity(name, layout, main, noTitle, fullscreen);
    }

    @CliCommand(value = VIEW_COMMAND, help = "Adds a view to a layout and binds it to the given type")
    public void view(@CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "identifier", mandatory = false, unspecifiedDefaultValue = "", help = "The view identifier (defaults to the field name)") final String identifier,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The type to receive this view") final JavaType typeName,
            @CliOption(key = "view", mandatory = true, help = "Class name of the view to create (package defaults to 'android.widget')") final String view,
            @CliOption(key = "height", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The height of the view group") final Dimension height,
            @CliOption(key = "width", mandatory = false, unspecifiedDefaultValue = "FILL_PARENT", help = "The width of the view group") final Dimension width) {
        projectOperations.view(typeName, view, identifier, fieldName, height, width);
    }

    @CliCommand(value = RESOURCE_STRING_COMMAND, help = "Creates a string resource")
    public void resourceString(@CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "name", mandatory = false, unspecifiedDefaultValue = "", help = "The name of the resource to add (defaults to the field name)") final String name,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The type to receive this resource") final JavaType typeName,
            @CliOption(key = "value", mandatory = true, help = "The content of the string resource") final String value) {
        projectOperations.resourceString(typeName, name, fieldName, value);
    }

    @CliCommand(value = SYSTEM_SERVICE_COMMAND, help = "Binds a system service to the given type")
    public void systemService(@CliOption(key = { "", "name" }, mandatory = true, help = "The type of system service to add") final SystemService value,
            @CliOption(key = "fieldName", mandatory = false, help = "The name of the field to add (defaults to the service class name") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The type to receive this service") final JavaType typeName,
            @CliOption(key = "addPermissions", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Add typical permissions for the service to the manifest") final boolean addPermissions) {
        projectOperations.systemService(typeName, fieldName, value, addPermissions);
    }

    @CliCommand(value = PERMISSION_COMMAND, help = "Adds a permission to the Android manifest")
    public void permission(@CliOption(key = { "", "name" }, mandatory = true, help = "The type of permission to add") final Permission name) {
        projectOperations.permission(name);
    }
    
    @CliCommand(value = FRAGMENT_COMMAND, help = "Creates a new Android fragment")
    public void fragment(@CliOption(key = "class", optionContext = "update,project", mandatory = true, help = "Name of the fragment to create") final JavaType name,
            @CliOption(key = "layout", mandatory = false, unspecifiedDefaultValue = "", help = "Name of the fragment layout to create and inflate in onCreateView()") final String layout,
            @CliOption(key = "support", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Subclass Fragment from the Android Support Library") final boolean support) {
        projectOperations.fragment(name, layout, support);
    }
}
