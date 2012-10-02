package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import java.util.Arrays;
import java.util.List;

import __TOP_LEVEL_PACKAGE__.client.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.IsLeafProcessor;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.ProxyListNodeProcessor;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.ProxyNodeProcessor;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyPlace;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;

/**
 * The navigation tree located on the left of the app.
 */
public class NavigationTree extends Composite {

	public interface Resources extends CellTree.Resources {

	}

	public static class ProxyListNode {
		private final String nodeName;
		private final Class<? extends EntityProxy> proxyType;
		private final String parentId;

		public ProxyListNode(String nodeName, Class<? extends EntityProxy> proxyType, String parentId) {
			this.nodeName = nodeName;
			this.proxyType = proxyType;
			this.parentId = parentId;
		}

		public String getNodeName() {
			return nodeName;
		}

		public Class<? extends EntityProxy> getProxyClass() {
			return proxyType;
		}

		public String getParentId() {
			return parentId;
		}
	}

	public static class ProxyNode extends ProxyListNode {
		private final EntityProxyId<?> proxyId;
		private final String serverId;

		public ProxyNode(String nodeName, EntityProxyId<?> proxyId, String serverId, String parentId) {
			super(nodeName, proxyId.getProxyClass(), parentId);
			this.proxyId = proxyId;
			this.serverId = serverId;
		}

		public EntityProxyId<?> getProxyId() {
			return proxyId;
		}

		public String getServerId() {
			return serverId;
		}
	}

	private static class ProxyNodeCell extends AbstractCell<ProxyNode> {
		@Override
		public void render(Cell.Context context, ProxyNode value, SafeHtmlBuilder sb) {
			sb.appendEscaped(value.getNodeName());
		}
	}

	private static class ProxyListNodeCell extends AbstractCell<ProxyListNode> {
		@Override
		public void render(Cell.Context context, ProxyListNode value, SafeHtmlBuilder sb) {
			sb.appendEscaped(value.getNodeName());
		}
	}

	/**
	 * The {@link TreeViewModel} that backs the navigation tree.
	 */
	private class NavigationTreeViewModel implements TreeViewModel {

		private final Cell<ProxyNode> proxyNodeCell = new ProxyNodeCell();
		private final Cell<ProxyListNode> proxyListNodeCell = new ProxyListNodeCell();

		private final ProxyListNodeProcessor proxyListNodeProcessor = new ProxyListNodeProcessor();
		private final ProxyNodeProcessor proxyNodeProcessor = new ProxyNodeProcessor(requestFactory);

		private final IsLeafProcessor isLeafProcessor = new IsLeafProcessor();

		public <T> NodeInfo<?> getNodeInfo(T value) {
			if (value == null) {
				// Top level.
				return new DefaultNodeInfo<ProxyListNode>(topLevelDataProvider, proxyListNodeCell, selectionModel, null);

			} else if (value instanceof ProxyNode) {
				ProxyNode proxyNode = (ProxyNode) value;

				ListDataProvider<ProxyListNode> dataProvider = new ListDataProvider<ProxyListNode>();
				proxyListNodeProcessor.process(dataProvider.getList(), proxyNode.getServerId(), proxyNode.getProxyClass());

				return new DefaultNodeInfo<ProxyListNode>(dataProvider, proxyListNodeCell, selectionModel, null);

			} else if (value instanceof ProxyListNode) {
				ProxyListNode listNode = (ProxyListNode) value;

				AbstractDataProvider<ProxyNode> dataProvider = proxyNodeProcessor.process(listNode.getParentId(), listNode.getProxyClass());

				if (dataProvider != null) {
					return new DefaultNodeInfo<ProxyNode>(dataProvider, proxyNodeCell, selectionModel, null);
				}
			}
			return null;
		}

		@Override
		public boolean isLeaf(Object value) {
			if (value instanceof ProxyNode) {
				return isLeafProcessor.process(((ProxyNode) value).getProxyClass());
			}
			return false;
		}
	}

	/**
	 * The data provider that provides the root nodes.
	 */
	private final ListDataProvider<ProxyListNode> topLevelDataProvider = new ListDataProvider<ProxyListNode>();

	/**
	 * The factory used to send requests.
	 */
	private final ApplicationRequestFactory requestFactory;

	/**
	 * In charge of the user's location in the app.
	 */
	private final PlaceController placeController;

	/**
	 * The shared {@link SingleSelectionModel}.
	 */
	private final SingleSelectionModel<ProxyListNode> selectionModel = new SingleSelectionModel<ProxyListNode>(new ProvidesKey<ProxyListNode>() {
		@Override
		public Object getKey(ProxyListNode node) {
			String key = requestFactory.getHistoryToken(node.getProxyClass());
			if (node.getParentId() != null) {
				key += node.getParentId();
			}
			if (node instanceof ProxyNode) {
				key += requestFactory.getHistoryToken(((ProxyNode) node).getProxyId());
			}
			return key;
		}
	});

	/**
	 * The main widget.
	 */
	private CellTree tree;

	@Inject
	public NavigationTree(ApplicationRequestFactory requestFactory, PlaceController placeController) {
		this.requestFactory = requestFactory;
		this.placeController = placeController;

		// Initialize the widget.
		createTree();
		initWidget(tree);
		getElement().getStyle().setOverflow(Overflow.AUTO);
	}

	public void addRootNodes(ProxyListNode... nodes) {
		final List<ProxyListNode> rootNodes = topLevelDataProvider.getList();
		rootNodes.addAll(Arrays.asList(nodes));
	}

	/**
	 * Create the {@link CellTree}.
	 */
	private void createTree() {
		final NavigationTreeViewModel model = new NavigationTreeViewModel();

		// Add before the CellBrowser adds its own handler.
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				Object selected = selectionModel.getSelectedObject();

				if (selected instanceof ProxyNode) {
					ProxyNode proxyNode = (ProxyNode) selected;
					ProxyPlace proxyPlace = new ProxyPlace(proxyNode.getProxyId(), proxyNode.getParentId());
					placeController.goTo(proxyPlace);
				} else if (selected instanceof ProxyListNode) {
					ProxyListNode proxyListNode = (ProxyListNode) selected;
					ProxyListPlace proxyListPlace = new ProxyListPlace(proxyListNode.getProxyClass(), proxyListNode.getParentId());
					placeController.goTo(proxyListPlace);
				}
			}
		});

		tree = new CellTree(model, null);
		tree.setAnimationEnabled(true);
	}
}