package org.springframework.roo.addon.requestfactory.tailor;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.tailor.CommandTransformation;
import org.springframework.roo.addon.tailor.actions.AbstractAction;
import org.springframework.roo.addon.tailor.actions.ActionConfig;

@Component
@Service
public class ExecutePrepend extends AbstractAction {

    public static final String ACTION_ATTR_PREFIX = "prefix";

    @Override
    protected void executeImpl(CommandTransformation command, ActionConfig config) {
        final String prefix = config.getAttribute(ACTION_ATTR_PREFIX);
        command.addOutputCommand(prefix + " " + command.getInputCommand());
    }

    @Override
    public String getDescription(ActionConfig config) {
        final String prefix = config.getAttribute(ACTION_ATTR_PREFIX);
        return "Executing original command with prefix: " + prefix;
    }

    @Override
    public boolean isValid(ActionConfig config) {
        return config != null;
    }
}
