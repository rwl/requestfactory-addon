package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import __TOP_LEVEL_PACKAGE__.client.scaffold.ScaffoldMobileApp;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.AbstractProxyListView;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListView;
import __TOP_LEVEL_PACKAGE__.client.managed.ApplicationMessages;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.github.gwtbootstrap.client.ui.Button;
import com.googlecode.mgwt.ui.client.widget.HeaderButton;
import com.googlecode.mgwt.ui.client.widget.HeaderPanel;

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

    @UiField(provided = true) CellList<P> list;
    @UiField HeaderPanel header;
    @UiField HeaderButton backButton;
    @UiField HTML title;
    @UiField HeaderButton newButton;

    /**
     * Constructor.
     *
     * @param buttonText the text to display on the create button
     * @param renderer   the {@link SafeHtmlRenderer} used to render the proxy
     */
    public MobileProxyListView(String buttonText, final SafeHtmlRenderer<P> renderer) {
        // Create the CellList to display the proxies.
        AbstractCell<P> cell = new AbstractCell<P>() {
            @Override
            public void render(Context context, P value, SafeHtmlBuilder sb) {
                renderer.render(value, sb);
            }
        };
        this.list = new CellList<P>(cell, ScaffoldMobileApp.getMobileListResources());
        
        // Initialize the widget.
        init(BINDER.createAndBindUi(this), list, newButton, backButton);
        
        this.newButton.setText(messages.create());
        this.backButton.setText(messages.back());
    }

    public Widget getBackButton() {
        return backButton;
    }
}
