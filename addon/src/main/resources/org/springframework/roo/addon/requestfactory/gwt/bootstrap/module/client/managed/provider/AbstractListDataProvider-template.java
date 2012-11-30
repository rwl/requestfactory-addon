package __TOP_LEVEL_PACKAGE__.managed.provider;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;


/**
 * Abstract {@link AsyncDataProvider} used for EntityProxy lists.
 */
public abstract class AbstractListDataProvider<T extends EntityProxy, S> extends AsyncDataProvider<S> {

    public AbstractListDataProvider() {
        super(null);
    }

    @Override
    public void addDataDisplay(HasData<S> display) {
        super.addDataDisplay(display);

        // Request the count anytime a view is added.
        getCountRequest().fire(new Receiver<Long>() {
            @Override
            public void onSuccess(Long response) {
                updateRowCount(response.intValue(), true);
            }
        });
    }

    @Override
    protected void onRangeChanged(HasData<S> view) {
        Range range = view.getVisibleRange();
        getEntitiesRequest(range).fire(new Receiver<List<T>>() {
            @Override
            public void onSuccess(List<T> response) {
                List<S> values = new ArrayList<S>();
                for (T proxy : response) {
                    values.add(getDataValue(proxy));
                }
                updateRowData(/*range.getStart()*/0, values);
            }
        });
    }

    protected abstract Request<Long> getCountRequest();

    protected abstract Request<List<T>> getEntitiesRequest(Range range);

    protected abstract S getDataValue(T proxy);
}
