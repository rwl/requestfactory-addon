package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.application.Application;
import __TOP_LEVEL_PACKAGE__.ui.RootPlaceView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedEvent;
import com.googlecode.mgwt.ui.client.widget.celllist.CellSelectedHandler;

/**
 * The root activity that shows all entities.
 */
public class RootActivity extends AbstractActivity {

    private final PlaceController placeController;

    private final RootPlaceView rootPlaceView;

    private HandlerRegistration registration;

    @Inject
    public RootActivity(PlaceController placeController) {
        this.placeController = placeController;
        rootPlaceView = RootPlaceView.instance();
        rootPlaceView.setPlaces(Application.getRootPlaces());
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        if (registration != null) {
            registration.removeHandler();
            registration = null;
        }

        registration = rootPlaceView.getCellSelectedHandler().addCellSelectedHandler(new CellSelectedHandler() {
            @Override
            public void onCellSelected(CellSelectedEvent event) {
                int index = event.getIndex();

                Place place = Application.getRootPlaces().get(index);
                placeController.goTo(place);
            }
        });

        panel.setWidget(rootPlaceView);
    }

    @Override
    public void onStop() {
        if (registration != null) {
            registration.removeHandler();
            registration = null;
        }
    }
}
