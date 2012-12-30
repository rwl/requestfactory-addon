package org.springframework.roo.addon.requestfactory.android.project;

import java.util.Arrays;
import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.tailor.RequestFactoryActionConfigFactory;
import org.springframework.roo.addon.tailor.actions.ActionConfigFactory;
import org.springframework.roo.addon.tailor.config.CommandConfiguration;
import org.springframework.roo.addon.tailor.config.TailorConfiguration;
import org.springframework.roo.addon.tailor.config.TailorConfigurationFactory;

@Component
@Service
public class AndroidProjectTailorConfiguration implements TailorConfigurationFactory {

    @Override
    public List<TailorConfiguration> createTailorConfiguration() {
        TailorConfiguration config = new TailorConfiguration("android",
                "Develop projects for the Android operating system");
        config.addCommandConfig(createCommandConfigProjectSetup());
        for (final String command : AndroidProjectCommands.ANDROID_COMMANDS) {
            final String abbrev = command.substring(AndroidProjectCommands
                    .ANDROID_PREFIX.length() + 1);
            config.addCommandConfig(createCommandConfigPrefixSetup(abbrev));
        }
        return Arrays.asList(config);
    }

    private CommandConfiguration createCommandConfigProjectSetup() {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName("project");
        config.addAction(ActionConfigFactory.defaultArgumentAction(
                "packaging", "APK"));
        config.addAction(ActionConfigFactory.executeAction());
        return config;
    }

    private CommandConfiguration createCommandConfigPrefixSetup(
            final String commandName) {
        CommandConfiguration config = new CommandConfiguration();
        config.setCommandName(commandName);
        config.addAction(RequestFactoryActionConfigFactory
                .executePrefixedAction(AndroidProjectCommands.ANDROID_PREFIX));
        return config;
    }
}
