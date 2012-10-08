package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.stereotype.Service;

import __FULL_ACCOUNT_NAME__;
import __FULL_ROLE_NAME__;

/**
 * UserDetailsService implementation which accepts any OpenID user, "registering" new users if they have not logged in previously.
 */
//@Service("registeringUserService")
public class AccountService implements UserDetailsService, AuthenticationUserDetailsService<OpenIDAuthenticationToken> {

	/**
	 * Implementation of {@code UserDetailsService}. We only need this to
	 * satisfy the {@code RememberMeServices} requirements.
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			return __ACCOUNT_NAME__.find__ACCOUNT_NAME__ByUsername(username);
		} catch (NoResultException e) {
			throw new UsernameNotFoundException(username);
		} catch (EmptyResultDataAccessException e) {
			throw new UsernameNotFoundException(username);
		}
	}

	/**
	 * Implementation of {@code AuthenticationUserDetailsService} which
	 * allows full access to the submitted {@code Authentication} object.
	 * Used by the OpenIDAuthenticationProvider.
	 */
	@Override
	public UserDetails loadUserDetails(OpenIDAuthenticationToken token)
	        throws UsernameNotFoundException {
		String id = token.getIdentityUrl();

		try {
			return __ACCOUNT_NAME__.find__ACCOUNT_NAME__ByUsername(id);
		} catch (NoResultException e) {
		} catch (EmptyResultDataAccessException e) {
		}

		String email = "";
		String firstName = "";
		String lastName = "";
		String fullName = "";

		List<OpenIDAttribute> attributes = token.getAttributes();

		for (OpenIDAttribute attribute : attributes) {
			if (attribute.getName().equals("email")) {
				email = attribute.getValues().get(0);
			}
			if (attribute.getName().equals("firstname")) {
				firstName = attribute.getValues().get(0);
			}
			if (attribute.getName().equals("lastname")) {
				lastName = attribute.getValues().get(0);
			}
			if (attribute.getName().equals("fullname")) {
				fullName = attribute.getValues().get(0);
			}
		}

		if (fullName == null) {
			StringBuilder fullNameBldr = new StringBuilder();

			if (firstName != null) {
				fullNameBldr.append(firstName);
				if (lastName != null) {
					fullNameBldr.append(" ");
				}
			}
			if (lastName != null) {
				fullNameBldr.append(lastName);
			}
			fullName = fullNameBldr.toString();
		}

		__ACCOUNT_NAME__ account = new __ACCOUNT_NAME__();
		account.setUsername(id);
		account.getUserRoles().add(Role.ROLE_USER);
		account.setEmail(email);
		account.setName(fullName);
		account.persist();

		return account;
	}
}