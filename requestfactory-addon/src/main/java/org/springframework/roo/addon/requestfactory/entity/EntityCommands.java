package org.springframework.roo.addon.requestfactory.entity;

import java.util.Set;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class EntityCommands implements CommandMarker {

    private static final String SCAFFOLD_COMMAND = "web requestfactory scaffold entity";
    private static final String PLURAL_COMMAND = "plural";
    private static final String TO_STRING_COMMAND = "toString";

    @Reference private EntityOperations operations;

    @CliAvailabilityIndicator({ SCAFFOLD_COMMAND, PLURAL_COMMAND, TO_STRING_COMMAND })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    @CliCommand(value = SCAFFOLD_COMMAND, help = "Configure entity for GWT Bootstrap")
    public void add(@CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", help = "The entity to configure") JavaType target,
            @CliOption(key = RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE, mandatory = false, help = "The name of the field of the parent") final JavaSymbolName parentProperty,
            @CliOption(key = RooRequestFactory.PRIMARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Primary property to be used when rendering") final JavaSymbolName primaryProperty,
            @CliOption(key = RooRequestFactory.SECONDARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Secondary property to be used when rendering") final JavaSymbolName secondaryProperty) {
        operations.annotateType(target, parentProperty, primaryProperty, secondaryProperty);
    }

    @CliCommand(value = PLURAL_COMMAND, help = "Configure the plural of a particular type")
    public void add(@CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", help = "The entity to configure") JavaType target,
            @CliOption(key = "name", mandatory = true, help = "The plural name to use when working with this type") final String name) {
        operations.annotateTypeWithPlural(target, name);
    }

    @CliCommand(value = TO_STRING_COMMAND, help = "Provides a toString() method")
    public void add(@CliOption(key = "type", mandatory = false, unspecifiedDefaultValue = "*", help = "The entity to configure") JavaType target,
            @CliOption(key = "excludeFields", mandatory = false, optionContext = "exclude-fields", help = "Fields to exclude in the toString method. Multiple field names must be a double-quoted list separated by spaces") final Set<String> excludeFields,
            @CliOption(key = "method", mandatory = false, help = "The name of the method to generate, defaults to toString") final String methodName) {
        operations.annotateTypeWithToString(target, excludeFields, methodName);
    }
}