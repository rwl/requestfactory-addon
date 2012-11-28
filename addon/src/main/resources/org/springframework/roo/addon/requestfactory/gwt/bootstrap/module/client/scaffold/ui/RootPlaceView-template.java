package __TOP_LEVEL_PACKAGE__.client.scaffold.ui;

import java.util.List;

import __TOP_LEVEL_PACKAGE__.client.managed.ApplicationMessages;
import __TOP_LEVEL_PACKAGE__.client.managed.ui.renderer.ApplicationListPlaceRenderer;
import __TOP_LEVEL_PACKAGE__.client.scaffold.place.ProxyListPlace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.celllist.CellListWithHeader;
import com.googlecode.mgwt.ui.client.widget.celllist.HasCellSelectedHandler;


public class RootPlaceView extends Composite {

	private static RootPlaceViewUiBinder uiBinder = GWT.create(RootPlaceViewUiBinder.class);

	interface RootPlaceViewUiBinder extends UiBinder<Widget, RootPlaceView> {
	}

	private static final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

	private static RootPlaceView instance;

	public static RootPlaceView instance() {
		if (instance == null) {
			instance = new RootPlaceView();
		}
		return instance;
	}
	private final Renderer<ProxyListPlace> listPlaceRenderer = new ApplicationListPlaceRenderer();

	@UiField(provided = true)
	CellListWithHeader<ProxyListPlace> list;

	@UiField
	HTML title;

	public RootPlaceView() {
		list = new CellListWithHeader<ProxyListPlace>(new BasicCell<ProxyListPlace>() {

			@Override
			public String getDisplayString(ProxyListPlace model) {
				return listPlaceRenderer.render(model);
			}
		});

		initWidget(uiBinder.createAndBindUi(this));

		title.setText(messages.rootTitle());
	}

	public void setPlaces(List<ProxyListPlace> places) {
		list.getCellList().render(places);
	}

	public HasCellSelectedHandler getCellSelectedHandler() {
		return list.getCellList();
	}
}
