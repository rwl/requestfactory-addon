package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window.Location;

/**
 * A minimal auth failure handler which takes the user to a login page.
 */
public class LoginOnAuthenticationFailure implements AccountAuthenticationFailureEvent.Handler {

    public HandlerRegistration register(EventBus eventBus) {
        return AccountAuthenticationFailureEvent.register(eventBus, this);
    }

    public void onAuthFailure(AccountAuthenticationFailureEvent requestEvent) {
        Location.replace(requestEvent.getLoginUrl());
    }
}
