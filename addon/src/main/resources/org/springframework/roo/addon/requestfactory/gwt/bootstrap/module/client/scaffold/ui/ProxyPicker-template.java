package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import java.util.HashSet;
import java.util.Set;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Modal;
import com.github.gwtbootstrap.client.ui.ModalFooter;
import com.github.gwtbootstrap.client.ui.constants.BackdropType;
import com.github.gwtbootstrap.client.ui.constants.ButtonType;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.EntityProxy;


public class ProxyPicker<P extends EntityProxy> extends Modal {

    private static ProxyPickerUiBinder uiBinder = GWT.create(ProxyPickerUiBinder.class);

    interface ProxyPickerUiBinder extends UiBinder<Widget, ProxyPicker<?>> {
    }

    @UiField ShowMorePagerPanel showMorePagerPanel;
    @UiField RangeLabelPager rangeLabelPager;

    private final Button addButton = new Button("Add", IconType.PLUS, new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            if (callback == null) {
                return;
            }
            if (singleSelection) {
                Set<P> tmp = new HashSet<P>();
                tmp.add(singleSelectionModel.getSelectedObject());
                callback.callback(tmp);
            } else {
                callback.callback(multiSelectionModel.getSelectedSet());
            }
        }
    });

    private final Button closeButton = new Button("Add & Close", new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            if (callback == null) {
                return;
            }
            if (singleSelection) {
                Set<P> tmp = new HashSet<P>();
                tmp.add(singleSelectionModel.getSelectedObject());
                callback.callback(tmp);
            } else {
                callback.callback(multiSelectionModel.getSelectedSet());
            }
            ProxyPicker.this.hide();
        }
    });

    private final CellList<P> cellList;

    private final SingleSelectionModel<P> singleSelectionModel = new SingleSelectionModel<P>(new EntityProxyKeyProvider<P>());

    private final MultiSelectionModel<P> multiSelectionModel = new MultiSelectionModel<P>(new EntityProxyKeyProvider<P>());

    private final boolean singleSelection;

    private final CollectionCallback<P> callback;

    private AbstractDataProvider<P> dataProvider;

    public ProxyPicker(final Renderer<P> renderer, final String title,
            final boolean singleSelection,
            final CollectionCallback<P> callback) {
        super(false, true);

        this.singleSelection = singleSelection;
        this.callback = callback;

        setTitle(title);
        setCloseVisible(true);
        setBackdrop(BackdropType.STATIC);
        setWidth(330);

        add(uiBinder.createAndBindUi(this));
        add(new ModalFooter(addButton, closeButton));

        addButton.setType(ButtonType.PRIMARY);
        closeButton.setType(ButtonType.INFO);

        cellList = new CellList<P>(new DefaultCell<P>(renderer),
                CellListResources.INSTANCE,
                new EntityProxyKeyProvider<P>());
        cellList.setPageSize(30);
        cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
        cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        if (singleSelection) {
            cellList.setSelectionModel(singleSelectionModel);
            singleSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                public void onSelectionChange(SelectionChangeEvent event) {
                    addButton.setEnabled(singleSelectionModel.getSelectedObject() != null);
                }
            });
        } else {
            cellList.setSelectionModel(multiSelectionModel);
            multiSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                public void onSelectionChange(SelectionChangeEvent event) {
                    addButton.setEnabled(!multiSelectionModel.getSelectedSet().isEmpty());
                }
            });
        }

        showMorePagerPanel.setDisplay(cellList);
        rangeLabelPager.setDisplay(cellList);
    }

    public void setProvider(AbstractDataProvider<P> provider) {
        if (dataProvider != null) {
            dataProvider.removeDataDisplay(cellList);
        }
        provider.addDataDisplay(cellList);
        dataProvider = provider;
    }
}
