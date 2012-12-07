package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Implemented by views that visualize an object.
 *
 * @param <P> the type of object to show
 */
public interface ProxyVisualizeView<P> extends TakesValue<P>, IsWidget {

    /**
     * Implemented by the owner of the view.
     */
    interface Delegate {

        void editClicked();
    }
}
