package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.shared.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.client.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.client.scaffold.ui.NavigationTree;
import __TOP_LEVEL_PACKAGE__.client.managed.ApplicationMessages;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.github.gwtbootstrap.client.ui.NavText;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 * The outermost UI of the application.
 */
public class ScaffoldDesktopShell extends Composite {

	interface Binder extends UiBinder<Widget, ScaffoldDesktopShell> {
	}

	private static final Binder BINDER = GWT.create(Binder.class);

    private final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

	@UiField SimplePanel details;
	@UiField HasText nickname;
	@UiField HasClickHandlers signout;
	@UiField SimplePanel master;
	@UiField NotificationMole mole;
	@UiField(provided = true)
	NavigationTree navigationTree;

    @Inject
	public ScaffoldDesktopShell(ApplicationRequestFactory requestFactory, PlaceController placeController) {
        this.navigationTree = new NavigationTree(requestFactory, placeController);
		initWidget(BINDER.createAndBindUi(this));
		nickname.setText(messages.notSignedIn());
		mole.setMessage(messages.loading());
	}

	/**
	 * @return the panel to hold the details
	 */
	public SimplePanel getDetailsPanel() {
		return details;
	}

	/**
	 * @return the panel to hold the master list
	 */
	public SimplePanel getMasterPanel() {
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
