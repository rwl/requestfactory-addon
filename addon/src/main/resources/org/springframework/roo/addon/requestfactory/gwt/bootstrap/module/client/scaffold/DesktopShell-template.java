package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.Set;
import java.util.HashSet;
import java.util.Date;

import __SHARED_TOP_LEVEL_PACKAGE__.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.ui.NavigationTree;
import __TOP_LEVEL_PACKAGE__.messages.ApplicationMessages;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import com.github.gwtbootstrap.client.ui.Dropdown;
import com.github.gwtbootstrap.client.ui.NavText;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.Brand;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.inject.Inject;
import com.googlecode.mgwt.mvp.client.AnimatableDisplay;

/**
 * The outermost UI of the application.
 */
public class DesktopShell extends Composite {

    interface Binder extends UiBinder<Widget, DesktopShell> {
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
    @UiField Dropdown localeSelect;
    @UiField SimplePanel north;

    private final Set<NavLink> navLinks = new HashSet<NavLink>();

    @Inject
    public DesktopShell(ApplicationRequestFactory requestFactory, EventBus eventBus, PlaceController placeController) {
        this.navigationTree = new NavigationTree(requestFactory, placeController, eventBus, breadcrumbs);
        initWidget(BINDER.createAndBindUi(this));
        initLocaleSelect();
        // https://github.com/gwtbootstrap/gwt-bootstrap/issues/231
        north.getElement().getParentElement().getStyle().setOverflow(Overflow.VISIBLE);
        brand.setText(messages.applicationName());
        nickname.setText(messages.notSignedIn());
        mole.setMessage(messages.loading());
        splitPanel.setWidgetMinSize(navigationTree, 150);
        splitPanel.setWidgetSnapClosedSize(navigationTree, 120);
    }

    private void initLocaleSelect() {
        final String cookieName = LocaleInfo.getLocaleCookieName();
        final String queryParam = LocaleInfo.getLocaleQueryParam();
        if (cookieName == null && queryParam == null) {
            localeSelect.setVisible(false);
            return;
        }
        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();
        if (currentLocale.equals("default")) {
            currentLocale = "en";
        }
        final String[] localeNames = LocaleInfo.getAvailableLocaleNames();
        for (final String localeName : localeNames) {
            if (localeName.equals("default")) {
                continue;
            }
            final String nativeName = LocaleInfo
                    .getLocaleNativeDisplayName(localeName);
            final NavLink navLink = new NavLink(nativeName);
            navLink.setName(localeName);
            navLinks.add(navLink);
            localeSelect.add(navLink);
            if (localeName.equals(currentLocale)) {
                navLink.setIcon(IconType.CHECK);
            } else {
                navLink.setIcon(IconType.CHECK_EMPTY);
            }
            navLink.addClickHandler(new ClickHandler() {

                @SuppressWarnings("deprecation")
                @Override
                public void onClick(ClickEvent event) {
                    final String localeName = navLink.getName();
                    if (cookieName != null) {
                        final Date expires = new Date();
                        expires.setYear(expires.getYear() + 1);
                        Cookies.setCookie(cookieName, localeName, expires);
                    }

                    for (final NavLink link : navLinks) {
                        link.setIcon(IconType.CHECK_EMPTY);
                    }
                    navLink.setIcon(IconType.CHECK);

                    if (queryParam != null) {
                        final UrlBuilder builder = Location.createUrlBuilder()
                                .setParameter(queryParam, localeName);
                        Window.Location.replace(builder.buildString());
                    } else {
                        Window.Location.reload();
                    }
                }
            });
        }
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
