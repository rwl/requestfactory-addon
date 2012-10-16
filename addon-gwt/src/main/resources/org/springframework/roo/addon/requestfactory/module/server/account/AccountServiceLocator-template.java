package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

__IMPORT_ACCOUNT__
__IMPORT_ROLE__
import com.google.web.bindery.requestfactory.shared.ServiceLocator;

/**
 * Gives a RequestFactory system access to the Spring Security UserDetails service.
 */
public class AccountServiceLocator implements ServiceLocator {

	public AccountServiceWrapper getInstance(Class<?> clazz) {

		return new AccountServiceWrapper() {

			@Override
			public Account getAccount() {
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				return (Account) auth.getPrincipal();
			}

			@Override
			public Boolean isAdminAccount() {
				Account account = getAccount();
				return (account != null) ? account.getUserRoles().contains(Role.ROLE_ADMIN) : false;
			}

			@Override
			public String getIdentityUrl() {
				Account account = getAccount();
				return (account != null) ? account.getUsername() : "";
			}

			@Override
			public String getName() {
				Account account = getAccount();
				return (account != null) ? account.getName() : "";
			}

			@Override
			public String getEmail() {
				Account account = getAccount();
				return (account != null) ? account.getEmail() : "";
			}

			@Override
			public String getAccountId() {
				Account account = getAccount();
				return (account != null) ? com.google.appengine.api.datastore.KeyFactory.keyToString(account.getId()) : "";
			}
		};
	}
}
