package __TOP_LEVEL_PACKAGE__.client.managed.tree;

import java.util.ArrayList;
import java.util.List;

import __TOP_LEVEL_PACKAGE__.client.scaffold.ui.NavigationTree;
import __TOP_LEVEL_PACKAGE__.client.scaffold.ui.NavigationTree.ProxyNode;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;


/**
 * Abstract {@link ListDataProvider} used for EntityProxy lists.
 */
public abstract class AbstractListDataProvider<T extends EntityProxy> extends AsyncDataProvider<ProxyNode> {

	protected final String parentId;

	public AbstractListDataProvider(String parentId) {
		super(null);
		this.parentId = parentId;
	}

	@Override
	public void addDataDisplay(HasData<ProxyNode> display) {
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
	protected void onRangeChanged(HasData<ProxyNode> view) {
		Range range = view.getVisibleRange();
		getEntitiesRequest(range).fire(new Receiver<List<T>>() {
			@Override
			public void onSuccess(List<T> response) {
				List<ProxyNode> values = new ArrayList<ProxyNode>();
				for (T proxy : response) {
					values.add(new ProxyNode(getNodeName(proxy), proxy.stableId(), getServerId(proxy), parentId));
				}
				updateRowData(/*range.getStart()*/0, values);
			}
		});
	}

	protected abstract Request<Long> getCountRequest();

	protected abstract Request<List<T>> getEntitiesRequest(Range range);

	protected abstract String getNodeName(T proxy);

	protected abstract String getServerId(T proxy);
}