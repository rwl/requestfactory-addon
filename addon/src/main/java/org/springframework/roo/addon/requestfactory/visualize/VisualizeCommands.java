package org.springframework.roo.addon.requestfactory.visualize;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class VisualizeCommands implements CommandMarker {

    private static final String MAP_MARKER_COMMAND = "web requestfactory map marker";
    private static final String SETUP_MAP_COMMAND = "web requestfactory gwt bootstrap maps setup";

    @Reference private VisualizeOperations operations;

    @CliAvailabilityIndicator({ MAP_MARKER_COMMAND })
    public boolean isScaffoldAvailable() {
        return operations.isAnnotateCommandAvailable();
    }

    @CliAvailabilityIndicator({ SETUP_MAP_COMMAND })
    public boolean isGwtSetupAvailable() {
        return operations.isSetupCommandAvailable();
    }

    @CliCommand(value = SETUP_MAP_COMMAND, help = "Setup project for displaying maps")
    public void setupMapsGwt() {
        operations.setupMapsGwt();
    }

    @CliCommand(value = MAP_MARKER_COMMAND, help = "Creates map scaffold for the specified type")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created map marker scaffold on") final JavaType type,
            @CliOption(key = RooMapMarker.LAT_FIELD_ATTRIBUTE, mandatory = false, help = "Field name for marker latitude") final String lat,
            @CliOption(key = RooMapMarker.LON_FIELD_ATTRIBUTE, mandatory = false, help = "Field name for marker longitude") final String lon) {
        operations.annotateNodeType(type, lat, lon);
    }
}