package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.web.bindery.requestfactory.shared.ProxyForName;
import com.google.web.bindery.requestfactory.shared.ValueProxy;

/**
 * Client visible proxy of the Account class.
 */
@ProxyForName("__FULL_ACCOUNT_NAME__")
public interface OpenIdAccount extends ValueProxy {

	String getEmail();

	String getName();
}
