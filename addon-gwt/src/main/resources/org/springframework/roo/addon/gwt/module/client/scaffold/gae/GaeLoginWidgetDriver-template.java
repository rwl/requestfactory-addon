package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.shared.gae.GaeUser;
import __TOP_LEVEL_PACKAGE__.shared.gae.GaeUserServiceRequest;
import __TOP_LEVEL_PACKAGE__.shared.gae.MakesGaeRequests;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Makes GAE requests to drive a LoginWidget.
 */
public class GaeLoginWidgetDriver {
	private final MakesGaeRequests requests;

	public GaeLoginWidgetDriver(MakesGaeRequests requests) {
		this.requests = requests;
	}

	public void setWidget(final HasText hasText, final HasClickHandlers hasClickHandlers) {
		GaeUserServiceRequest request = requests.userServiceRequest();
		request.createLogoutURL(Location.getHref()).to(new Receiver<String>() {
			public void onSuccess(final String response) {
				hasClickHandlers.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Location.replace(response);
					}
				});
			}
		});

		request.getCurrentUser().to(new Receiver<GaeUser>() {
			@Override
			public void onSuccess(GaeUser response) {
				if (response != null) {
					hasText.setText(response.getNickname());
				}
			}
		});

		request.fire();
	}
}
