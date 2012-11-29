package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import __TOP_LEVEL_PACKAGE__.client.scaffold.ScaffoldMobileApp;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.AbstractProxyListView;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListView;
import __TOP_LEVEL_PACKAGE__.client.managed.ApplicationMessages;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.googlecode.mgwt.ui.client.widget.HeaderPanel;
import com.googlecode.mgwt.ui.client.widget.base.ButtonBase;

/**
 * An implementation of {@link ProxyListView} used in mobile applications
 *
 * @param <P> the type of the proxy
 */
public abstract class MobileProxyListView<P extends EntityProxy> extends AbstractProxyListView<P> {

    interface Binder extends UiBinder<Widget, MobileProxyListView<?>> {
    }

    private static final Binder BINDER = GWT.create(Binder.class);

    private ApplicationMessages messages = GWT.create(ApplicationMessages.class);

    private final CellList<P> list;

    @UiField ShowMorePagerPanel showMorePagerPanel;
    @UiField HeaderPanel header;
    @UiField ButtonBase backButton;
    @UiField HTML title;
    @UiField ButtonBase newButton;

    /**
     * Constructor.
     *
     * @param buttonText the text to display on the create button
     * @param renderer   the {@link SafeHtmlRenderer} used to render the proxy
     */
    public MobileProxyListView(final String buttonText, final String titleText, final Renderer<P> renderer) {
        list = new CellList<P>(new DefaultCell<P>(renderer), CellListResources.INSTANCE, new EntityProxyKeyProvider<P>());
        list.setPageSize(8);
        list.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
        
        // Initialize the widget.
        init(BINDER.createAndBindUi(this), list, newButton, backButton);

        title.setText(titleText);
        backButton.setText(messages.back());

        showMorePagerPanel.setDisplay(list);
    }

    public Widget getBackButton() {
        return backButton;
    }
}
