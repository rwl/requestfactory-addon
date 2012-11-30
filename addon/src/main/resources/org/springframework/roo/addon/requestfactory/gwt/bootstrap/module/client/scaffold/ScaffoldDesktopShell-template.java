package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.ui.NavigationTree;
import __TOP_LEVEL_PACKAGE__.managed.ApplicationMessages;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.github.gwtbootstrap.client.ui.NavText;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Brand;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;
import com.googlecode.mgwt.mvp.client.AnimatableDisplay;

/**
 * The outermost UI of the application.
 */
public class ScaffoldDesktopShell extends Composite {

    interface Binder extends UiBinder<Widget, ScaffoldDesktopShell> {
    }

    private static final Binder BINDER = GWT.create(Binder.class);

    private final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

    @UiField HasText nickname;
    @UiField HasClickHandlers signout;
    @UiField(provided = true)
    AnimatableDisplay master = GWT.create(AnimatableDisplay.class);
    @UiField NotificationMole mole;
    @UiField(provided = true)
    NavigationTree navigationTree;
    @UiField SplitLayoutPanel splitPanel;
    @UiField(provided = true)
    Breadcrumbs breadcrumbs = new Breadcrumbs();
    @UiField Brand brand;

    @Inject
    public ScaffoldDesktopShell(ApplicationRequestFactory requestFactory, EventBus eventBus, PlaceController placeController) {
        this.navigationTree = new NavigationTree(requestFactory, placeController, eventBus, breadcrumbs);
        initWidget(BINDER.createAndBindUi(this));
        brand.setText(messages.applicationName());
        nickname.setText(messages.notSignedIn());
        mole.setMessage(messages.loading());
        splitPanel.setWidgetMinSize(navigationTree, 150);
        splitPanel.setWidgetSnapClosedSize(navigationTree, 120);
    }

    /**
     * @return the panel to hold the master list
     */
    public AnimatableDisplay getMasterPanel() {
        return master;
    }

    /**
     * @return the notification mole for loading feedback
     */
    public NotificationMole getMole() {
        return mole;
    }

    /**
     * @return the navigator
     */
    public NavigationTree getNavigationTree() {
        return navigationTree;
    }

    /**
     * @return the nickname widget
     */
    public HasText getNicknameWidget() {
        return nickname;
    }

    /**
     * @return the sign out widget
     */
    public HasClickHandlers getSignOutWidget() {
        return signout;
    }
}
