package org.springframework.roo.addon.requestfactory.tailor;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.RequestFactoryCommands;
import org.springframework.roo.addon.requestfactory.android.AndroidCommands;
import org.springframework.roo.addon.requestfactory.entity.EntityCommands;
import org.springframework.roo.addon.requestfactory.entity.EntityFieldCommands;
import org.springframework.roo.addon.requestfactory.gwt.GwtCommands;
import org.springframework.roo.addon.requestfactory.visualize.VisualizeCommands;
import org.springframework.roo.addon.tailor.actions.ActionConfigFactory;
import org.springframework.roo.addon.tailor.config.CommandConfiguration;
import org.springframework.roo.addon.tailor.config.TailorConfiguration;
import org.springframework.roo.addon.tailor.config.TailorConfigurationFactory;

@Component
@Service
public class RequestFactoryTailorConfiguration implements TailorConfigurationFactory {

    private static final String VERSION = "0.1.0.BUILD-SNAPSHOT";

    @Override
    public List<TailorConfiguration> createTailorConfiguration() {
        final TailorConfiguration config = new TailorConfiguration(
                "requestfactory",
                "Project with multiple RequestFactory client modules");
        config.addCommandConfig(createCommandConfigProjectSetup());
        
        config.addCommandConfig(createServerFocusCommandConfig("jpa setup"));
        config.addCommandConfig(createServerFocusCommandConfig("mongo setup"));
        config.addCommandConfig(createServerFocusCommandConfig("entity"));
        config.addCommandConfig(createServerFocusCommandConfig("repository"));
        config.addCommandConfig(createServerFocusCommandConfig("service"));
        config.addCommandConfig(createModuleFocusCommandConfig("enum type", "shared"));
        
        config.addCommandConfig(createCommandConfigFieldSetup());
        config.addCommandConfig(createProxyRequestCommandConfig());
        config.addCommandConfig(createPrefixedProxyRequestCommandConfig());
        
        config.addCommandConfig(createModuleFocusCommandConfig("web requestfactory gwt", "client/gwt"));
        config.addCommandConfig(createModuleFocusCommandConfig("web requestfactory android", "client/android"));
        
        for (final String command : RequestFactoryCommands.TAILORED_COMMANDS) {
            config.addCommandConfig(createCommandConfigPrefixSetup(command));
        }
        for (final String command : EntityCommands.TAILORED_COMMANDS) {
            config.addCommandConfig(createCommandConfigPrefixSetup(command));
        }
        for (final String command : VisualizeCommands.TAILORED_COMMANDS) {
            config.addCommandConfig(createCommandConfigPrefixSetup(command));
        }
        for (final String command : GwtCommands.TAILORED_COMMANDS) {
            config.addCommandConfig(createCommandConfigPrefixSetup(command, "client/gwt"));
        }
        for (final String command : AndroidCommands.TAILORED_COMMANDS) {
            config.addCommandConfig(createCommandConfigPrefixSetup(command, "client/android"));
        }
        return Arrays.asList(config);
    }

    private CommandConfiguration createCommandConfigProjectSetup() {
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("project");
        config.addAction(ActionConfigFactory.defaultArgumentAction("packaging", "POM_SIMPLE"));
        config.addAction(ActionConfigFactory.defaultArgumentAction("projectName", "${topLevelPackage}"));
        config.addAction(ActionConfigFactory.executeAction());
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup addon"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName server --topLevelPackage ${topLevelPackage}.server --artifactId ${projectName}-server --parent ${topLevelPackage}:${projectName}:" + VERSION));
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup server"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --artifactId spring-tx --groupId org.springframework --version ${spring.version}"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --artifactId spring-web --groupId org.springframework --version ${spring.version}"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage}.shared --artifactId ${projectName}-shared --version " + VERSION));
        config.addAction(ActionConfigFactory.executeAction("logging setup --level INFO"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName shared --topLevelPackage ${topLevelPackage}.shared --artifactId ${projectName}-shared --parent ${topLevelPackage}:${projectName}:" + VERSION + " --packaging JAR_SIMPLE"));
        config.addAction(ActionConfigFactory.executeAction("web requestfactory setup server"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName client --topLevelPackage ${topLevelPackage}.client --artifactId ${projectName}-client --parent ${topLevelPackage}:${projectName}:" + VERSION + " --packaging POM_SIMPLE"));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName client/gwt --topLevelPackage ${topLevelPackage}.client.gwt --artifactId ${projectName}-gwt --parent ${topLevelPackage}.client:${projectName}-client:" + VERSION + " --packaging JAR_SIMPLE"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage}.shared --artifactId ${projectName}-shared --version " + VERSION));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage}.server --artifactId ${projectName}-server --version " + VERSION));
        config.addAction(ActionConfigFactory.focusModuleAction("~"));
        config.addAction(ActionConfigFactory.executeAction("module create --moduleName client/android --topLevelPackage ${topLevelPackage}.client.android --artifactId ${projectName}-android --parent ${topLevelPackage}.client:${projectName}-client:" + VERSION + " --packaging APK"));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage}.shared --artifactId ${projectName}-shared --version " + VERSION));
        config.addAction(ActionConfigFactory.executeAction("dependency add --groupId ${topLevelPackage}.server --artifactId ${projectName}-server --version " + VERSION + " --scope PROVIDED"));
        return config;
    }

    private CommandConfiguration createModuleFocusCommandConfig(
            final String commandName, final String moduleName) {
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName(commandName);
        config.addAction(ActionConfigFactory.focusModuleAction(moduleName));
        config.addAction(ActionConfigFactory.executeAction());
        return config;
    }

    private CommandConfiguration createServerFocusCommandConfig(
            final String commandName) {
        return createModuleFocusCommandConfig(commandName, "server");
    }

    private CommandConfiguration createCommandConfigFieldSetup() {
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("field");
        config.addAction(ActionConfigFactory.focusModuleAction("server"));
        config.addAction(RequestFactoryActionConfigFactory
                .executePrefixedAction(EntityFieldCommands
                        .REQUEST_FACTORY_FIELD_COMMAND_PREFIX));
        return config;
    }

    private CommandConfiguration createProxyRequestCommandConfig() {
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("proxy request all");
        config.addAction(ActionConfigFactory.defaultArgumentAction("serverModule", "server"));
        config.addAction(ActionConfigFactory.focusModuleAction("shared"));
        config.addAction(RequestFactoryActionConfigFactory
                .executePrefixedAction(RequestFactoryCommands
                        .WEB_REQUEST_FACTORY_PREFIX));
        return config;
    }

    private CommandConfiguration createPrefixedProxyRequestCommandConfig() {
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("web requestfactory proxy request all");
        config.addAction(ActionConfigFactory.defaultArgumentAction("serverModule", "server"));
        config.addAction(ActionConfigFactory.focusModuleAction("shared"));
        config.addAction(ActionConfigFactory.executeAction());
        return config;
    }

    private CommandConfiguration createCommandConfigPrefixSetup(
            final String commandName) {
        return createCommandConfigPrefixSetup(commandName, null);
    }

    private CommandConfiguration createCommandConfigPrefixSetup(
                final String commandName, final String moduleName) {
        final String abbrev = commandName.substring(RequestFactoryCommands
                .WEB_REQUEST_FACTORY_PREFIX.length() + 1);
        final CommandConfiguration config = new CommandConfiguration();
        config.setCommandName(abbrev);
        if (!StringUtils.isEmpty(moduleName)) {
            config.addAction(ActionConfigFactory.focusModuleAction(moduleName));
        }
        config.addAction(RequestFactoryActionConfigFactory
                .executePrefixedAction(RequestFactoryCommands
                        .WEB_REQUEST_FACTORY_PREFIX));
        return config;
    }
}
