package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.shared.EntityProxy;


public class CollectionEditor<P extends EntityProxy> extends Modal {

    private static CollectionEditorUiBinder uiBinder = GWT.create(CollectionEditorUiBinder.class);

    interface CollectionEditorUiBinder extends UiBinder<Widget, CollectionEditor<?>> {
    }

    @UiField SimplePanel panel;

    @UiField Button delete;
    @UiField Button moveToTop;
    @UiField Button moveUp;
    @UiField Button moveDown;
    @UiField Button moveToBottom;

    private final Button ok = new Button("OK", IconType.OK, new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            if (callback != null) {
                Set<P> tmp = new HashSet<P>();
                tmp.add(selectionModel.getSelectedObject());
                callback.callback(tmp);
            }
            CollectionEditor.this.hide();
        }
    });

    private final Button cancel = new Button("Cancel", IconType.REMOVE, new ClickHandler() {

        @Override
        public void onClick(ClickEvent event) {
            CollectionEditor.this.hide();
        }
    });

    private final CellList<P> cellList;

    private final SingleSelectionModel<P> selectionModel;

    private final boolean ordered;

    private final CollectionCallback<P> callback;

    private ListDataProvider<P> dataProvider;

    private List<P> backingList;

    public CollectionEditor(Renderer<P> renderer, final String title,
            final boolean ordered,
            final CollectionCallback<P> callback) {
        super(false, true);

        this.ordered = ordered;
        this.callback = callback;

        setTitle(title);
        setCloseVisible(true);
        setBackdrop(BackdropType.STATIC);
        setWidth(400);

        add(uiBinder.createAndBindUi(this));
        add(new ModalFooter(ok, cancel));

        ok.setType(ButtonType.PRIMARY);

        cellList = new CellList<P>(new DefaultCell<P>(renderer),
                CellListResources.INSTANCE, new EntityProxyKeyProvider<P>());
        cellList.setPageSize(30);
        cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
        cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

        selectionModel = new SingleSelectionModel<P>(new EntityProxyKeyProvider<P>());
        cellList.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selectionChanged();
            }
        });

        panel.add(cellList);

        selectionChanged();
    }

    private void selectionChanged() {
        final P selected = selectionModel.getSelectedObject();
        delete.setEnabled(selected != null);
        if (ordered) {
            final boolean up = (selected != null
                && !selected.equals(backingList.get(0)));
            final boolean down = (selected != null
                && !selected.equals(backingList.get(backingList.size() - 1)));
            moveToTop.setEnabled(up);
            moveUp.setEnabled(up);
            moveDown.setEnabled(down);
            moveToBottom.setEnabled(down);
        } else {
            moveToTop.setEnabled(false);
            moveUp.setEnabled(false);
            moveDown.setEnabled(false);
            moveToBottom.setEnabled(false);
        }
    }

    @UiHandler("moveToTop")
    public void moveToTopClicked(ClickEvent e) {
        if (backingList == null) {
            return;
        }
        final P selected = selectionModel.getSelectedObject();
        int i = backingList.indexOf(selected);
        while (i > 0) {
            Collections.swap(backingList, i, i - 1);
            i = backingList.indexOf(selected);
        }
        selectionChanged();
    }

    @UiHandler("moveUp")
    public void moveUpClicked(ClickEvent e) {
        if (backingList == null) {
            return;
        }
        final P selected = selectionModel.getSelectedObject();
        final int i = backingList.indexOf(selected);
        if (i > 0) {
            Collections.swap(backingList, i, i - 1);
        }
        selectionChanged();
    }

    @UiHandler("moveToBottom")
    public void moveToBottom(ClickEvent e) {
        if (backingList == null) {
            return;
        }
        final P selected = selectionModel.getSelectedObject();
        int i = backingList.indexOf(selected);
        while (i < backingList.size() - 1) {
            Collections.swap(backingList, i, i + 1);
            i = backingList.indexOf(selected);
        }
        selectionChanged();
    }

    @UiHandler("moveDown")
    public void moveDown(ClickEvent e) {
        if (backingList == null) {
            return;
        }
        final P selected = selectionModel.getSelectedObject();
        final int i = backingList.indexOf(selected);
        if (i < backingList.size() - 1) {
            Collections.swap(backingList, i, i + 1);
        }
        selectionChanged();
    }

    @UiHandler("delete")
    public void deleteClicked(ClickEvent e) {
        final P selected = selectionModel.getSelectedObject();
        if (selected != null && backingList != null) {
            selectionModel.setSelected(selected, false);
            backingList.remove(selected);
            dataProvider.refresh();
        }
        selectionChanged();
    }

    public void setProvider(ListDataProvider<P> provider) {
        if (dataProvider != null) {
            dataProvider.removeDataDisplay(cellList);
        }
        provider.addDataDisplay(cellList);
        dataProvider = provider;
        backingList = dataProvider.getList();
        selectionChanged();
    }
}
