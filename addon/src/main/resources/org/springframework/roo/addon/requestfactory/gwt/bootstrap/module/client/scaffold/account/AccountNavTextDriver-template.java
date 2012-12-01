package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.proxy.AccountProxy;
import __TOP_LEVEL_PACKAGE__.account.MakesAccountRequests;
import __TOP_LEVEL_PACKAGE__.account.OpenIdAccountServiceRequest;
import __TOP_LEVEL_PACKAGE__.messages.ApplicationMessages;

import com.google.gwt.user.client.ui.HasText;
import com.google.web.bindery.requestfactory.shared.Receiver;

/**
 * Makes account requests to drive a HasText.
 */
public class AccountNavTextDriver {
	private final MakesAccountRequests requests;

    private final ApplicationMessages messages = GWT.create(ApplicationMessages.class);

	public AccountNavTextDriver(MakesAccountRequests requests) {
		this.requests = requests;
	}

	public void setWidget(final HasText widget) {
		OpenIdAccountServiceRequest request = requests.accountServiceRequest();

		request.getAccount().to(new Receiver<AccountProxy>() {
			@Override
			public void onSuccess(AccountProxy account) {
				if (account != null) {
					String identifier;
					if (account.getName() != null && account.getName().length() > 0) {
						identifier = account.getName();
					} else if (account.getEmail() != null && account.getEmail().length() > 0) {
						identifier = account.getEmail();
					} else if (account.getUsername() != null && account.getUsername().length() > 0) {
						identifier = account.getUsername();
					} else {
						identifier = "";
					}
					widget.setText(messages.signedInAs(identifier));
				} else {
				    widget.setText(messages.notSignedIn());
				}
			}
		});

		request.fire();
	}
}
