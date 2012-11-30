package __TOP_LEVEL_PACKAGE__.ui;

import java.util.ArrayList;
import java.util.Collection;

import __TOP_LEVEL_PACKAGE__.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.place.ProxyListPlace;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasConstrainedValue;
import com.google.gwt.user.client.ui.ValuePicker;

public class ProxyListPlaceValuePicker extends Composite implements HasConstrainedValue<ProxyListPlace> {

	public static interface CellListResources extends CellList.Resources {

		public static CellListResources INSTANCE = GWT.create(CellListResources.class);

		/**
		 * The styles used in this widget.
		 */
		@Source(CellListStyle.DEFAULT_CSS)
		CellList.Style cellListStyle();
	}

	public static interface CellListStyle extends CellList.Style {

		/**
		 * The path to the default CSS styles used by this resource.
		 */
		String DEFAULT_CSS = "ProxyListPlaceValuePicker.css";

		/**
		 * Applied to selected items.
		 */
		String cellListSelectedItem();
	}

	private static class DefaultCell<T> extends AbstractCell<T> {
		private final Renderer<T> renderer;

		DefaultCell(Renderer<T> renderer) {
			this.renderer = renderer;
		}

		@Override
		public void render(Context context, T value, SafeHtmlBuilder sb) {
			sb.appendEscaped(renderer.render(value));
		}
	}

	private static __TOP_LEVEL_PACKAGE__.ui.ProxyListPlaceValuePicker instance;

	public static __TOP_LEVEL_PACKAGE__.ui.ProxyListPlaceValuePicker instance() {
		if (instance == null) {
			instance = new ProxyListPlaceValuePicker();
		}
		return instance;
	}

	private final CellList<ProxyListPlace> cellList;

	private final ValuePicker<ProxyListPlace> valueBox;

	public ProxyListPlaceValuePicker() {
		this(new ApplicationListPlaceRenderer());
	}

	public ProxyListPlaceValuePicker(Renderer<ProxyListPlace> renderer) {

		cellList = new CellList<ProxyListPlace>(new DefaultCell<ProxyListPlace>(renderer), CellListResources.INSTANCE);

		valueBox = new ValuePicker<ProxyListPlace>(cellList);

		initWidget(valueBox);
	}

	public int size() {
		return cellList.getRowCount();
	}

	public void setAcceptableValues(Collection<ProxyListPlace> places) {
		valueBox.setAcceptableValues(places);
		cellList.setRowCount(new ArrayList<ProxyListPlace>(places).size());
	}

	public void setPageSize(int pageSize) {
		valueBox.setPageSize(pageSize);
	}

	@Override
	public ProxyListPlace getValue() {
		return valueBox.getValue();
	}

	@Override
	public void setValue(ProxyListPlace value) {
		valueBox.setValue(value);
	}

	@Override
	public void setValue(ProxyListPlace value, boolean fireEvents) {
		valueBox.setValue(value, fireEvents);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<ProxyListPlace> handler) {
		return valueBox.addValueChangeHandler(handler);
	}
}