package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.googlecode.mgwt.dom.client.event.touch.HasTouchHandlers;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartEvent;
import com.googlecode.mgwt.dom.client.event.touch.TouchStartHandler;

/**
 * Abstract implementation of ProxyListView.
 *
 * @param <P> the type of the proxy
 */
public abstract class AbstractProxyListView<P extends EntityProxy> extends Composite implements ProxyListView<P> {
    private HasData<P> display;
    private ProxyListView.Delegate<P> delegate;

    public HasData<P> asHasData() {
        return display;
    }

    @Override
    public AbstractProxyListView<P> asWidget() {
        return this;
    }

    public void setDelegate(final Delegate<P> delegate) {
        this.delegate = delegate;
    }

    protected void init(Widget root, HasData<P> display, HasClickHandlers newButton, HasClickHandlers backButton, HasClickHandlers viewButton) {
        super.initWidget(root);
        this.display = display;

        newButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                delegate.createClicked();
            }
        });

        if (backButton != null) {
            backButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    delegate.backClicked();
                }
            });
        }

        if (viewButton != null) {
            viewButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    delegate.viewClicked();
                }
            });
        }
    }

    protected void init(Widget root, HasData<P> display, HasTouchHandlers newButton, HasTouchHandlers backButton) {
        super.initWidget(root);
        this.display = display;

        newButton.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent event) {
                delegate.createClicked();
            }
        });

        backButton.addTouchStartHandler(new TouchStartHandler() {
            @Override
            public void onTouchStart(TouchStartEvent event) {
                delegate.backClicked();
            }
        });
    }

    protected void initWidget(Widget widget) {
        throw new UnsupportedOperationException("AbstractProxyListView must be initialized via init(Widget, HasData<P>, Button) ");
    }
}
