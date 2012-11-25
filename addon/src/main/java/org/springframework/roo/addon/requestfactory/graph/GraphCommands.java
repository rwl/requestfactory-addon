package org.springframework.roo.addon.requestfactory.graph;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class GraphCommands implements CommandMarker {

    private static final String GRAPH_NODE_COMMAND = "web requestfactory graph node";
    private static final String SETUP_GRAPH_COMMAND = "web requestfactory graph setup";

    @Reference private GraphOperations operations;

    @CliAvailabilityIndicator({ GRAPH_NODE_COMMAND })
    public boolean isScaffoldAvailable() {
        return operations.isAnnotateCommandAvailable();
    }

    @CliAvailabilityIndicator({ SETUP_GRAPH_COMMAND })
    public boolean isGwtSetupAvailable() {
        return operations.isSetupCommandAvailable();
    }

    @CliCommand(value = SETUP_GRAPH_COMMAND, help = "Install Google Maps API v3 into your project")
    public void webGwtBootstrapSetup() {
        operations.setupGraph();
    }

    @CliCommand(value = GRAPH_NODE_COMMAND, help = "Creates graph scaffold for the specified type")
    public void scaffoldType(@CliOption(key = "type", mandatory = true, help = "The type to base the created graph node scaffold on") final JavaType type,
            @CliOption(key = RooGraphNode.X_FIELD_ATTRIBUTE, mandatory = false, help = "The x-coordinate field name") final String x,
            @CliOption(key = RooGraphNode.Y_FIELD_ATTRIBUTE, mandatory = false, help = "The y-coordinate field name") final String y) {
        operations.annotateNodeType(type, x, y);
    }
}