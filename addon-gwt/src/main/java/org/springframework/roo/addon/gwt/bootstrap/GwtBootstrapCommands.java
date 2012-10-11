package org.springframework.roo.addon.gwt.bootstrap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.operations.Cardinality;
import org.springframework.roo.classpath.operations.Fetch;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Sample of a command class. The command class is registered by the Roo shell following an
 * automatic classpath scan. You can provide simple user presentation-related logic in this
 * class. You can return any objects from each method, or use the logger directly if you'd
 * like to emit messages of different severity (and therefore different colours on
 * non-Windows systems).
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class GwtBootstrapCommands implements CommandMarker { // All command types must implement the CommandMarker interface

    /**
     * Get a reference to the GwtBootstrapOperations from the underlying OSGi container
     */
    @Reference private GwtBootstrapOperations operations;

    /**
     * This method is optional. It allows automatic command hiding in situations when the command should not be visible.
     * For example the 'entity' command will not be made available before the user has defined his persistence settings
     * in the Roo shell or directly in the project.
     *
     * You can define multiple methods annotated with {@link CliAvailabilityIndicator} if your commands have differing
     * visibility requirements.
     *
     * @return true (default) if the command should be visible at this stage, false otherwise
     */
    @CliAvailabilityIndicator({ "web gwtbootstrap entity" })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a mandatory command attribute.
     *
     * @param type
     */
    @CliCommand(value = "web gwtbootstrap entity", help = "Configure entity for GWT Bootstrap")
    public void add(@CliOption(key = "type", mandatory = true, help = "The entity to configure") JavaType target,
            @CliOption(key = RooGwtBootstrap.PARENT_PROPERTY_ATTRIBUTE, mandatory = false, help = "The name of the field of the parent") final JavaSymbolName parentProperty,
            @CliOption(key = RooGwtBootstrap.PRIMARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Primary property to be used when rendering") final JavaSymbolName primaryProperty,
            @CliOption(key = RooGwtBootstrap.SECONDARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Secondary property to be used when rendering") final JavaSymbolName secondaryProperty) {
        operations.annotateType(target, parentProperty, primaryProperty, secondaryProperty);
    }

    @CliCommand(value = "field list", help = "Adds a private List field to an existing Java source file (eg the 'one' side of a many-to-one)")
    public void addFieldSetJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The entity which will be contained within the Set") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "mappedBy", mandatory = false, help = "The field name on the referenced type which owns the relationship") final JavaSymbolName mappedBy,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "sizeMin", mandatory = false, help = "The minimum number of elements in the collection") final Integer sizeMin,
            @CliOption(key = "sizeMax", mandatory = false, help = "The maximum number of elements in the collection") final Integer sizeMax,
            @CliOption(key = "cardinality", mandatory = false, unspecifiedDefaultValue = "MANY_TO_MANY", specifiedDefaultValue = "MANY_TO_MANY", help = "The relationship cardinality at a JPA level") Cardinality cardinality,
            @CliOption(key = "fetch", mandatory = false, help = "The fetch semantics at a JPA level") final Fetch fetch,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords) {
        operations.addFieldListJpa(fieldName, fieldType, typeName, mappedBy, notNull, nullRequired, sizeMin, sizeMax, cardinality, fetch, comment, transientModifier, permitReservedWords);
    }
}