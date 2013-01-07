package org.springframework.roo.addon.requestfactory.rest;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

@Component
@Service
public class RestProjectCommands implements CommandMarker {
    
    public static final String DATA_REST_PREFIX = "data rest";

    private static final String SETUP_COMMAND = DATA_REST_PREFIX + " setup";
    private static final String RESOURCE_COMMAND = DATA_REST_PREFIX + " resource";
    
    @Reference private RestProjectOperations restProjectOperations;

    @CliAvailabilityIndicator({ SETUP_COMMAND })
    public boolean isSetupAvailable() {
        return restProjectOperations.isSetupAvailable();
    }

    @CliAvailabilityIndicator({ RESOURCE_COMMAND })
    public boolean isResourceAvailable() {
        return restProjectOperations.isRestResourceAvailable();
    }

    @CliCommand(value = SETUP_COMMAND, help = "Configure the focused project to use Spring Data REST")
    public void setup() {
        restProjectOperations.setup(); 
    }

    @CliCommand(value = RESOURCE_COMMAND, help = "Configures the export of repository resources")
    public void restResource(@CliOption(key = { "", "repository" }, optionContext = "update,project", mandatory = false, help = "Name of the repository to configure") final JavaType name,
            @CliOption(key = "hide", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the resource should not be exported") final boolean hide,
            @CliOption(key = "path", mandatory = false, unspecifiedDefaultValue = "", help = "The segment of the URL under which the repository is exposed") final String path,
            @CliOption(key = "rel", mandatory = false, unspecifiedDefaultValue = "", help = "The 'rel' attribute is displayed in the exported links") final String rel) {
        restProjectOperations.restResource(name, hide, path, rel);
    }
}
