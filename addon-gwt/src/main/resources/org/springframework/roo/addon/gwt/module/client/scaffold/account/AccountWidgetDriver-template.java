package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.client.proxy.AccountProxy;
import __TOP_LEVEL_PACKAGE__.shared.account.Account;
import __TOP_LEVEL_PACKAGE__.shared.account.OpenIdAccountServiceRequest;
import __TOP_LEVEL_PACKAGE__.shared.account.MakesAccountRequests;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Makes account requests to drive a HasText and a HasClickHandlers.
 */
public class AccountWidgetDriver {
	private final MakesAccountRequests requests;

	public AccountWidgetDriver(MakesAccountRequests requests) {
		this.requests = requests;
	}

	public void setWidget(final HasText hasText, final HasClickHandlers hasClickHandlers) {
		OpenIdAccountServiceRequest request = requests.accountServiceRequest();

		request.getLogoutURL(Location.getHref()).to(new Receiver<String>() {
			public void onSuccess(final String response) {
				hasClickHandlers.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						Window.Location.replace(response);
					}
				}
			}
		});

		request.getAccount().to(new Receiver<AccountProxy>() {
			@Override
			public void onSuccess(AccountProxy account) {
				if (account != null) {
					String identifier;
					if (account.getName() != null && !account.getName().isEmpty()) {
						identifier = account.getName();
					} else if (account.getEmail() != null && !account.getEmail().isEmpty()) {
						identifier = account.getEmail();
					} else if (account.getIdentityUrl() != null && !account.getIdentityUrl().isEmpty()) {
						identifier = account.getIdentityUrl();
					} else {
						identifier = "";
					}
					hasText.setText(identifier);
				}
			}
		});

		request.fire();
	}
}
