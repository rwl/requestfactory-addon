package org.springframework.roo.addon.requestfactory.tailor;

import org.springframework.roo.addon.tailor.actions.ActionConfig;

public class RequestFactoryActionConfigFactory {

    public static ActionConfig executePrefixedAction(final String prefix) {
        final ActionConfig config = new ActionConfig(
                ExecutePrepend.class.getSimpleName());
        config.setAttribute(ExecutePrepend.ACTION_ATTR_PREFIX, prefix);
        return config;
    }
}
