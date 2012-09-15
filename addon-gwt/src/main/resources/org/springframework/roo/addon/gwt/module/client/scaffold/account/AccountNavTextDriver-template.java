package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.client.proxy.AccountProxy;
import __TOP_LEVEL_PACKAGE__.shared.account.Account;
import __TOP_LEVEL_PACKAGE__.shared.account.OpenIdAccountServiceRequest;
import __TOP_LEVEL_PACKAGE__.shared.account.MakesAccountRequests;
import com.github.gwtbootstrap.client.ui.NavText;
import com.google.web.bindery.requestfactory.shared.Receiver;

/**
 * Makes account requests to drive a NavText.
 */
public class AccountNavTextDriver {
	private final MakesAccountRequests requests;

	public AccountNavTextDriver(MakesAccountRequests requests) {
		this.requests = requests;
	}

	public void setWidget(final NavText widget) {
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
					} else if (account.getIdentityUrl() != null && account.getIdentityUrl().length() > 0) {
						identifier = account.getIdentityUrl();
					} else {
						identifier = "";
					}
					widget.setText(identifier);
				}
			}
		});

		request.fire();
	}
}
