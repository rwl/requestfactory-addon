package org.springframework.roo.addon.requestfactory.tailor;

import java.util.Arrays;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.entity.EntityFieldCommands;
import org.springframework.roo.addon.tailor.config.TailorConfiguration;
import org.springframework.roo.addon.tailor.config.TailorConfigurationFactory;
import org.springframework.roo.addon.tailor.config.CommandConfiguration;
import org.springframework.roo.addon.tailor.actions.ActionConfigFactory;

@Component
@Service
public class RequestFactoryTailorConfiguration implements TailorConfigurationFactory {

    private static final String VERSION = "0.1.0.BUILD-SNAPSHOT";

    @Override
    public List<TailorConfiguration> createTailorConfiguration() {
        TailorConfiguration config = new TailorConfiguration("requestfactory",
                "Project with multiple RequestFactory client modules");
        config.addCommandConfig(createCommandConfigProjectSetup());
        config.addCommandConfig(createDomainFocusCommandConfig("jpa setup"));
        config.addCommandConfig(createDomainFocusCommandConfig("mongo setup"));
        config.addCommandConfig(createDomainFocusCommandConfig("entity"));
        config.addCommandConfig(createDomainFocusCommandConfig("enum type"));
        config.addCommandConfig(createDomainFocusCommandConfig("repository"));
        config.addCommandConfig(createDomainFocusCommandConfig("service"));
        config.addCommandConfig(createCommandConfigFieldSetup());
        config.addCommandConfig(createProxyRequestCommandConfig());
        config.addCommandConfig(createModuleFocusCommandConfig("web requestfactory gwt bootstrap", "ui/gwt"));
        config.addCommandConfig(createModuleFocusCommandConfig("web requestfactory android", "ui/android"));
        return Arrays.asList(config);
    }

    private CommandConfiguration createCommandConfigProjectSetup() {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("project");
        config.addAction(ActionConfigFactory.defaultArgumentAction("packaging", "PARENT_POM"));
        config.addAction(ActionConfigFactory.executeAction());
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup addon"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName domain --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-domain --parent ${topLevelPackage}:${projectName}:" + VERSION));
        config.addAction(ActionConfigFactory.executeAction("logging setup --level INFO"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName ui --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-ui --parent ${topLevelPackage}:${projectName}:" + VERSION + " --packaging PARENT_POM"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName ui/server --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-server --parent ${topLevelPackage}:${projectName}-ui:" + VERSION));
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup server"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage} --artifactId ${projectName}-domain --version " + VERSION));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName ui/shared --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-shared --parent ${topLevelPackage}:${projectName}-ui:" + VERSION));
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup client"));
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup server"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName ui/gwt --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-gwt --parent ${topLevelPackage}:${projectName}-ui:" + VERSION));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage} --artifactId ${projectName}-domain --version " + VERSION));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage} --artifactId ${projectName}-shared --version " + VERSION));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName ui/android --topLevelPackage ${topLevelPackage} --artifactId ${projectName}-android --parent ${topLevelPackage}:${projectName}-ui:" + VERSION + " --packaging APK"));
        return config;
    }

    private CommandConfiguration createModuleFocusCommandConfig(
            final String commandName, final String moduleName) {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName(commandName);
        config.addAction(ActionConfigFactory.focusModuleAction(moduleName));
        config.addAction(ActionConfigFactory.executeAction());
        return config;
    }

    private CommandConfiguration createDomainFocusCommandConfig(
            final String commandName) {
        return createModuleFocusCommandConfig(commandName, "domain");
    }

    private CommandConfiguration createCommandConfigFieldSetup() {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("field");
        config.addAction(ActionConfigFactory.focusModuleAction("domain"));
        config.addAction(RequestFactoryActionConfigFactory
                .executePrefixAction(EntityFieldCommands
                        .REQUEST_FACTORY_FIELD_COMMAND_PREFIX));
        return config;
    }

    private CommandConfiguration createProxyRequestCommandConfig() {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("web requestfactory proxy request all");
        config.addAction(ActionConfigFactory.defaultArgumentAction("serverModule", "ui/server"));
        config.addAction(ActionConfigFactory.focusModuleAction("ui/shared"));
        config.addAction(ActionConfigFactory.executeAction());
        return config;
    }
}
