package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;

/**
 * An event posted when an authentication failure is detected.
 */
public class AccountAuthenticationFailureEvent extends GwtEvent<AccountAuthenticationFailureEvent.Handler> {

    /**
     * Implemented by handlers of this type of event.
     */
    public interface Handler extends EventHandler {
        /**
         * Called when a {@link AccountAuthenticationFailureEvent} is fired.
         *
         * @param requestEvent a {@link AccountAuthenticationFailureEvent} instance
         */
        void onAuthFailure(AccountAuthenticationFailureEvent requestEvent);
    }

    private static final Type<Handler> TYPE = new Type<Handler>();

    /**
     * Register a {@link AccountAuthenticationFailureEvent.Handler} on an {@link EventBus}.
     *
     * @param eventBus the {@link EventBus}
     * @param handler  a {@link AccountAuthenticationFailureEvent.Handler}
     * @return a {@link HandlerRegistration} instance
     */
    public static HandlerRegistration register(EventBus eventBus, AccountAuthenticationFailureEvent.Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

    /**
     * Will only be non-null if this is an event of type {@link State#RECEIVED},
     * and the RPC was successful.
     */
    private final String loginUrl;

    /**
     * Constructs a new @{link RequestEvent}.
     *
     * @param state    a {@link State} instance
     * @param response a {@link Response} instance
     */
    public AccountAuthenticationFailureEvent(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public GwtEvent.Type<Handler> getAssociatedType() {
        return TYPE;
    }

    /**
     * Returns the URL the user can visit to reauthenticate.
     *
     * @return a {@link Response} instance
     */
    public String getLoginUrl() {
        return loginUrl;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onAuthFailure(this);
    }
}
