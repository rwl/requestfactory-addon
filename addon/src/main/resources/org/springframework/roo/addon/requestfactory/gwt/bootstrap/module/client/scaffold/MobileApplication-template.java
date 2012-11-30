package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.managed.activity.*;
import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.activity.RootActivity;
import __TOP_LEVEL_PACKAGE__.activity.ApplicationAnimationMapper;
import __TOP_LEVEL_PACKAGE__.place.*;
import __TOP_LEVEL_PACKAGE__.account.helper.AccountHelper;
import com.google.gwt.activity.shared.*;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.place.shared.*;
import com.google.web.bindery.requestfactory.shared.LoggingRequest;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryLogHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.mvp.client.AnimatableDisplay;
import com.googlecode.mgwt.mvp.client.AnimatingActivityManager;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
__ACCOUNT_IMPORT__

/**
 * Mobile application for browsing entities.
 */
public class MobileApplication extends Application {


    private static final Logger LOGGER = Logger.getLogger(ApplicationEntryPoint.class.getName());
    public static final Place ROOT_PLACE = new Place() {};

    private final AnimatableDisplay display = GWT.create(AnimatableDisplay.class);

    private final MobileActivityMapper mobileActivityMapper;
    private final ApplicationRequestFactory requestFactory;
    private final EventBus eventBus;
    private final PlaceController placeController;
    private final PlaceHistoryFactory placeHistoryFactory;

    @Inject
    public MobileApplication(ApplicationRequestFactory requestFactory, EventBus eventBus, PlaceController placeController, MobileActivityMapper mobileActivityMapper, PlaceHistoryFactory placeHistoryFactory, AccountHelper accountHelper) {
        this.requestFactory = requestFactory;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.mobileActivityMapper = mobileActivityMapper;
        this.placeHistoryFactory = placeHistoryFactory;
    }

    @Override
    public void run() {
        this.isMobile = true;

        init();

        /* Hide the loading message */
        final Element loading = Document.get().getElementById("loading");
        loading.getParentElement().removeChild(loading);

        RootPanel.get().add(display);
    }

    private void init() {
        if (LogConfiguration.loggingIsEnabled()) {
            /* Add remote logging handler */
            RequestFactoryLogHandler.LoggingRequestProvider provider = new RequestFactoryLogHandler.LoggingRequestProvider() {
                public LoggingRequest getLoggingRequest() {
                    return requestFactory.loggingRequest();
                }
            };
            Logger.getLogger("").addHandler(new RequestFactoryLogHandler(provider, Level.WARNING, new ArrayList<String>()));
        }

        /* Set viewport and other settings for mobile */
        MGWT.applySettings(MGWTSettings.getAppSetting());

        final Activity rootActivity = new RootActivity(placeController);

        mobileActivityMapper.setRootActivity(rootActivity);

        final ApplicationAnimationMapper appAnimationMapper = new ApplicationAnimationMapper();
        final AnimatingActivityManager activityManager = new AnimatingActivityManager(mobileActivityMapper, appAnimationMapper, eventBus);
        activityManager.setDisplay(display);

        /* Browser history integration */
        HistoryMapper mapper = GWT.create(HistoryMapper.class);
        mapper.setFactory(placeHistoryFactory);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(mapper);
        placeHistoryHandler.register(placeController, eventBus, ROOT_PLACE);
        placeHistoryHandler.handleCurrentHistory();
    }
}
