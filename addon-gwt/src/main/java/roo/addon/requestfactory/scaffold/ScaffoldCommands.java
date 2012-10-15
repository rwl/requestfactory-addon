package roo.addon.requestfactory.scaffold;

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
public class ScaffoldCommands implements CommandMarker {

    private static final String SCAFFOLD_COMMAND = "web requestfactory scaffold entity";

    @Reference private ScaffoldOperations operations;

    @CliAvailabilityIndicator({ SCAFFOLD_COMMAND })
    public boolean isCommandAvailable() {
        return operations.isCommandAvailable();
    }

    @CliCommand(value = SCAFFOLD_COMMAND, help = "Configure entity for GWT Bootstrap")
    public void add(@CliOption(key = "type", mandatory = true, help = "The entity to configure") JavaType target,
            @CliOption(key = RooRequestFactory.PARENT_PROPERTY_ATTRIBUTE, mandatory = false, help = "The name of the field of the parent") final JavaSymbolName parentProperty,
            @CliOption(key = RooRequestFactory.PRIMARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Primary property to be used when rendering") final JavaSymbolName primaryProperty,
            @CliOption(key = RooRequestFactory.SECONDARY_PROPERTY_ATTRIBUTE, mandatory = false, help = "Secondary property to be used when rendering") final JavaSymbolName secondaryProperty) {
        operations.annotateType(target, parentProperty, primaryProperty, secondaryProperty);
    }
}