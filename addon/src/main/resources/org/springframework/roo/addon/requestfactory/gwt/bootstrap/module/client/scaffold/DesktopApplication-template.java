package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.managed.activity.*;
import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.account.helper.AccountHelper;
import __TOP_LEVEL_PACKAGE__.place.*;
import __TOP_LEVEL_PACKAGE__.request.RequestEvent;
import __TOP_LEVEL_PACKAGE__.activity.ApplicationAnimationMapper;
import __TOP_LEVEL_PACKAGE__.ui.NavigationTree.ProxyListNode;
import __TOP_LEVEL_PACKAGE__.managed.ui.renderer.ApplicationListPlaceRenderer;
import com.google.gwt.activity.shared.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.place.shared.*;
import com.google.web.bindery.requestfactory.shared.LoggingRequest;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryLogHandler;
import com.googlecode.mgwt.mvp.client.AnimatingActivityManager;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
__ACCOUNT_IMPORT__

/**
 * Application for browsing entities.
 */
public class DesktopApplication extends Application {
    private static final Logger LOGGER = Logger.getLogger(ApplicationEntryPoint.class.getName());
    private final DesktopShell shell;
    private final ApplicationRequestFactory requestFactory;
    private final EventBus eventBus;
    private final PlaceController placeController;
    private final PlaceHistoryFactory placeHistoryFactory;
    private final ApplicationMasterActivities applicationMasterActivities;

    @Inject
    public DesktopApplication(DesktopShell shell, ApplicationRequestFactory requestFactory, EventBus eventBus, PlaceController placeController, PlaceHistoryFactory placeHistoryFactory, ApplicationMasterActivities applicationMasterActivities, AccountHelper accountHelper) {
        this.shell = shell;
        this.requestFactory = requestFactory;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.placeHistoryFactory = placeHistoryFactory;
        this.applicationMasterActivities = applicationMasterActivities;
    }

    public void run() {
        /* Add handlers, setup activities */
        init();

        /* Hide the loading message */
        Element loading = Document.get().getElementById("loading");
        loading.getParentElement().removeChild(loading);

        /* And show the user the shell */
        RootLayoutPanel.get().add(shell);
    }

    private void init() {
        if (LogConfiguration.loggingIsEnabled()) {
            // Add remote logging handler
            RequestFactoryLogHandler.LoggingRequestProvider provider = new RequestFactoryLogHandler.LoggingRequestProvider() {
                public LoggingRequest getLoggingRequest() {
                    return requestFactory.loggingRequest();
                }
            };
            Logger.getLogger("").addHandler(new RequestFactoryLogHandler(provider, Level.WARNING, new ArrayList<String>()));
        }

        RequestEvent.register(eventBus, new RequestEvent.Handler() {
            // Only show loading status if a request isn't serviced in 250ms.
            private static final int LOADING_TIMEOUT = 250;

            public void onRequestEvent(RequestEvent requestEvent) {
                if (requestEvent.getState() == RequestEvent.State.SENT) {
                    shell.getMole().showDelayed(LOADING_TIMEOUT);
                } else {
                    shell.getMole().hide();
                }
            }
        });

        final ApplicationAnimationMapper appAnimationMapper = new ApplicationAnimationMapper();
        final AnimatingActivityManager detailsManager = new AnimatingActivityManager(applicationMasterActivities, appAnimationMapper, eventBus);
        detailsManager.setDisplay(shell.getMasterPanel());

        /* Browser history integration */
        HistoryMapper mapper = GWT.create(HistoryMapper.class);
        mapper.setFactory(placeHistoryFactory);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(mapper);
        if (getRootPlaces().iterator().hasNext()) {
            ApplicationListPlaceRenderer renderer = new ApplicationListPlaceRenderer();
            for (ProxyListPlace place : getRootPlaces()) {
                shell.getNavigationTree().addRootNodes(new ProxyListNode(renderer.render(place),
                        place.getProxyClass(), null));
            }

            ProxyListPlace defaultPlace = getRootPlaces().iterator().next();
            placeHistoryHandler.register(placeController, eventBus, defaultPlace);
            placeHistoryHandler.handleCurrentHistory();
        }
    }
}
