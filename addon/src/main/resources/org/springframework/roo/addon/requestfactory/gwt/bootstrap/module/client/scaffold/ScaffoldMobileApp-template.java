package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.client.managed.activity.*;
import __SHARED_TOP_LEVEL_PACKAGE__.shared.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.client.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.client.scaffold.activity.RootActivity;
import __TOP_LEVEL_PACKAGE__.client.scaffold.activity.ScaffoldAnimationMapper;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.*;
import __TOP_LEVEL_PACKAGE__.client.scaffold.account.helper.AccountHelper;
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
public class ScaffoldMobileApp extends ScaffoldApp {


    private static final Logger LOGGER = Logger.getLogger(Scaffold.class.getName());
    public static final Place ROOT_PLACE = new Place() {};

    private final AnimatableDisplay display = GWT.create(AnimatableDisplay.class);

    private final ScaffoldMobileActivities scaffoldMobileActivities;
    private final ApplicationRequestFactory requestFactory;
    private final EventBus eventBus;
    private final PlaceController placeController;
    private final PlaceHistoryFactory placeHistoryFactory;

    @Inject
    public ScaffoldMobileApp(ApplicationRequestFactory requestFactory, EventBus eventBus, PlaceController placeController, ScaffoldMobileActivities scaffoldMobileActivities, PlaceHistoryFactory placeHistoryFactory, AccountHelper accountHelper) {
        this.requestFactory = requestFactory;
        this.eventBus = eventBus;
        this.placeController = placeController;
        this.scaffoldMobileActivities = scaffoldMobileActivities;
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

        scaffoldMobileActivities.setRootActivity(rootActivity);

        final ScaffoldAnimationMapper appAnimationMapper = new ScaffoldAnimationMapper();
        final AnimatingActivityManager activityManager = new AnimatingActivityManager(scaffoldMobileActivities, appAnimationMapper, eventBus);
        activityManager.setDisplay(display);

        /* Browser history integration */
        ScaffoldPlaceHistoryMapper mapper = GWT.create(ScaffoldPlaceHistoryMapper.class);
        mapper.setFactory(placeHistoryFactory);
        PlaceHistoryHandler placeHistoryHandler = new PlaceHistoryHandler(mapper);
        placeHistoryHandler.register(placeController, eventBus, ROOT_PLACE);
        placeHistoryHandler.handleCurrentHistory();
    }
}
