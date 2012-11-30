package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import __TOP_LEVEL_PACKAGE__.proxy.AccountProxy;
import __TOP_LEVEL_PACKAGE__.account.AccountServiceLocator;
import __TOP_LEVEL_PACKAGE__.account.AccountServiceWrapper;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.Service;

/**
 * Makes requests of the account service.
 */
@Service(value = AccountServiceWrapper.class, locator = AccountServiceLocator.class)
public interface OpenIdAccountServiceRequest extends RequestContext {

	Request<String> getIdentityUrl();

	Request<String> getName();

	Request<String> getEmail();

	Request<Boolean> isAdminAccount();

	Request<AccountProxy> getAccount();

	Request<String> getAccountId();
}
