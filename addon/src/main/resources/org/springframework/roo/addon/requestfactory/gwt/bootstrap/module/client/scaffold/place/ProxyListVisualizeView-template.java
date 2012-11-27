package __TOP_LEVEL_PACKAGE__.client.scaffold.place;

import java.util.List;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.requestfactory.shared.EntityProxy;

/**
 * A visualization of a list of {@link EntityProxy}s.
 */
public interface ProxyListVisualizeView<P extends EntityProxy> extends TakesValue<List<P>>, IsWidget {

    interface Delegate<R extends EntityProxy> {
    }
    /**
     * Sets the delegate.
     */
    void setDelegate(Delegate<P> delegate);

}
