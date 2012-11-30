package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import __SHARED_TOP_LEVEL_PACKAGE__.shared.managed.request.ApplicationRequestFactory;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.IsLeafProcessor;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.ProxyListNodeProcessor;
import __TOP_LEVEL_PACKAGE__.client.managed.tree.ProxyNodeProcessor;
import __TOP_LEVEL_PACKAGE__.client.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.client.managed.ui.renderer.ApplicationProxyPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListPlace;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyPlace;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyPlace.Operation;

import com.github.gwtbootstrap.client.ui.Breadcrumbs;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;
import com.google.gwt.user.cellview.client.TreeNode;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;

/**
 * The navigation tree located on the left of the app.
 */
public class NavigationTree extends Composite implements PlaceChangeEvent.Handler {

    public interface NavigationTreeResources extends CellTree.Resources {

        public static NavigationTreeResources INSTANCE = GWT.create(NavigationTreeResources.class);

        @Source("NavigationTree.css")
        public CellTree.Style cellTreeStyle();
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
            result = prime * result + ((proxyType == null) ? 0 : proxyType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProxyListNode other = (ProxyListNode) obj;
            if (parentId == null) {
                if (other.parentId != null)
                    return false;
            } else if (!parentId.equals(other.parentId))
                return false;
            if (proxyType == null) {
                if (other.proxyType != null)
                    return false;
            } else if (!proxyType.equals(other.proxyType))
                return false;
            return true;
        }
    }

    public static class ProxyNode extends ProxyListNode {
        private final ApplicationRequestFactory requests;
        private final EntityProxyId<?> proxyId;
        private final String serverId;

        public ProxyNode(String nodeName, Class<? extends EntityProxy> proxyType, EntityProxyId<?> proxyId, String serverId, String parentId, ApplicationRequestFactory requests) {
            super(nodeName, proxyType, parentId);
            this.requests = requests;
            this.proxyId = proxyId;
            this.serverId = serverId;
        }

        public EntityProxyId<?> getProxyId() {
            return proxyId;
        }

        public String getServerId() {
            return serverId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((proxyId == null) ? 0 : proxyId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            ProxyNode other = (ProxyNode) obj;
            if (proxyId == null) {
                if (other.proxyId != null)
                    return false;
            } else if (!requests.getHistoryToken(proxyId).equals(requests.getHistoryToken(other.proxyId)))
                return false;
            return true;
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
     * Dispatches Events to interested parties.
     */
    private final EventBus eventBus;

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
            if (node instanceof ProxyNode && ((ProxyNode) node).getProxyId() != null) {
                key += requestFactory.getHistoryToken(((ProxyNode) node).getProxyId());
            }
            return key;
        }
    });

    /**
     * The main widget.
     */
    private CellTree tree;

    private final Breadcrumbs breadcrumbs;

    private final ApplicationProxyPlaceRenderer proxyPlaceRenderer;

    private final ApplicationListPlaceRenderer listPlaceRenderer;

    public NavigationTree(ApplicationRequestFactory requestFactory, PlaceController placeController, EventBus eventBus, Breadcrumbs breadcrumbs) {
        this.requestFactory = requestFactory;
        this.placeController = placeController;
        this.eventBus = eventBus;
        this.breadcrumbs = breadcrumbs;

        this.proxyPlaceRenderer = new ApplicationProxyPlaceRenderer(requestFactory);
        this.listPlaceRenderer = new ApplicationListPlaceRenderer();

        this.eventBus.addHandler(PlaceChangeEvent.TYPE, this);

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

        tree = new CellTree(model, null, NavigationTreeResources.INSTANCE);
        tree.setAnimationEnabled(true);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final Place newPlace = event.getNewPlace();
        selectTreeNode(newPlace);
        updateBreadcrumbs(newPlace);
    }

    private void selectTreeNode(final Place newPlace) {
        final ProxyListNode selected = selectionModel.getSelectedObject();

        if (selected instanceof ProxyNode) {
            final ProxyNode proxyNode = (ProxyNode) selected;

            if (newPlace instanceof ProxyListPlace) {
                final ProxyListPlace proxyListPlace = (ProxyListPlace) newPlace;
                final ProxyListNode placeListNode = new ProxyListNode(null, proxyListPlace.getProxyClass(), proxyListPlace.getParentId());
                select(placeListNode);
            } else if (newPlace instanceof ProxyPlace) {
                final ProxyPlace proxyPlace = (ProxyPlace) newPlace;
                final ProxyNode placeNode = new ProxyNode(null, proxyPlace.getProxyClass(), proxyPlace.getProxyId(), null, proxyPlace.getParentId(), requestFactory);
                if (!proxyNode.equals(placeNode)) {
                    select(placeNode);
                }
            }
        } else {
            if (newPlace instanceof ProxyPlace) {
                final ProxyPlace proxyPlace = (ProxyPlace) newPlace;
                final ProxyNode placeNode = new ProxyNode(null, proxyPlace.getProxyClass(), proxyPlace.getProxyId(), null, proxyPlace.getParentId(), requestFactory);
                select(placeNode);
            } else if (newPlace instanceof ProxyListPlace) {
                final ProxyListPlace proxyListPlace = (ProxyListPlace) newPlace;
                final ProxyListNode placeListNode = new ProxyListNode(null, proxyListPlace.getProxyClass(), proxyListPlace.getParentId());
                if (!placeListNode.equals(selected)) {
                    select(placeListNode);
                }
            }
        }
    }

    private void select(ProxyListNode placeListNode) {
        selectionModel.setSelected(placeListNode, true);
    }

    private TreeNode getSelectedParentTreeNode(TreeNode treeNode, ProxyListNode selected) {
        if (selected == null) {
            return null;
        }
        for (int i = 0; i < treeNode.getChildCount(); i++) {
            final ProxyListNode childValue = (ProxyListNode) treeNode.getChildValue(i);
            if (selected.equals(childValue)) {
                return treeNode;
            } else if (treeNode.isChildOpen(i)) {
                final TreeNode childNode = treeNode.setChildOpen(i, true, false);
                final TreeNode selectedNode = getSelectedParentTreeNode(childNode, selected);
                if (selectedNode != null) {
                    return selectedNode;
                }
            }
        }
        return null;
    }

    private void updateBreadcrumbs(final Place newPlace) {
        final TreeNode rootNode = tree.getRootTreeNode();
        final TreeNode selectedParentTreeNode = getSelectedParentTreeNode(rootNode, selectionModel.getSelectedObject());

        final List<Widget> widgets = new ArrayList<Widget>();

        final NavLink leafLink = new NavLink();
        if (newPlace instanceof ProxyPlace) {
            final ProxyPlace proxyPlace = (ProxyPlace) newPlace;
            if (!proxyPlace.getOperation().equals(Operation.CREATE)) {
                proxyPlaceRenderer.render(proxyPlace, breadcrumbs, leafLink);
            } else {
                leafLink.setText("Create");
                widgets.add(leafLink);
            }
        } else if (newPlace instanceof ProxyListPlace) {
            final ProxyListPlace proxyListPlace = (ProxyListPlace) newPlace;
            leafLink.setText(listPlaceRenderer.render(proxyListPlace));
            widgets.add(leafLink);
        }

        if (selectedParentTreeNode != null) {
            prependBreadcrumb(selectedParentTreeNode, widgets);
        } else {
            if (newPlace instanceof ProxyPlace) {
                final ProxyPlace proxyPlace = (ProxyPlace) newPlace;

                final ProxyListPlace proxyListPlace = new ProxyListPlace(proxyPlace.getProxyClass(), proxyPlace.getParentId());
                final NavLink navLink = new NavLink(listPlaceRenderer.render(proxyListPlace));
                navLink.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        placeController.goTo(proxyListPlace);
                    }
                });
                widgets.add(0, navLink);
            }
        }

        breadcrumbs.clear();
        for (Widget widget : widgets) {
            breadcrumbs.add(widget);
        }
    }

    private void prependBreadcrumb(final TreeNode treeNode, final List<Widget> widgets) {
        assert treeNode != null;
        ProxyListNode value = (ProxyListNode) treeNode.getValue();
        if (value != null) {
            final NavLink navLink = new NavLink(value.getNodeName());
            final Place place;
            if (value instanceof ProxyNode) {
                final ProxyNode proxyNode = (ProxyNode) value;
                place = new ProxyPlace(proxyNode.getProxyId(), proxyNode.getParentId());
            } else {
                place = new ProxyListPlace(value.getProxyClass(), value.getParentId());
            }
            navLink.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    placeController.goTo(place);
                }
            });
            widgets.add(0, navLink);

            final TreeNode parentNode = treeNode.getParent();
            if (parentNode != null) {
                prependBreadcrumb(parentNode, widgets);
            }
        }
    }
}
